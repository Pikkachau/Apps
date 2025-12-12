package com.mcmlr.system.products.warps

import com.artillexstudios.axplayerwarps.AxPlayerWarps
//import com.artillexstudios.axplayerwarps.api.AxPlayerWarpsAPI
import com.mcmlr.blocks.api.Resources
import com.mcmlr.blocks.api.data.ConfigModel
import com.mcmlr.blocks.api.data.Repository
import com.mcmlr.system.dagger.AppScope
import com.mcmlr.system.products.data.CooldownRepository
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.*
import javax.inject.Inject

@AppScope
class WarpsRepository @Inject constructor(
    private val resources: Resources,
    private val cooldownRepository: CooldownRepository,
    private val warpsConfigRepository: WarpsConfigRepository,
) : Repository<ServerWarpsModel>(resources.dataFolder()) {

    private var updatingWarp: WarpModel? = null
    
    // Lazy load the API to ensure the plugin is enabled before we grab the instance
    private val axApi: AxPlayerWarpsAPI by lazy {
        AxPlayerWarps.getApi()
    }

    init {
        // We no longer load from a local file, so we initialize an empty dummy model
        // to satisfy the parent Repository class.
        model = ServerWarpsModel(listOf())
    }

    // --- INTEGRATION: Teleport using AxPlayerWarps ---
    fun teleport(player: Player, warpName: String) {
        val warp = axApi.warpManager.getWarp(warpName)
        if (warp != null) {
            // The plugin handles the actual teleport logic (and its own internal cooldowns if configured)
            warp.teleport(player)
            
            // If you still want to track your OWN custom cooldown logic from your CooldownRepository:
            cooldownRepository.addPlayerLastWarpTime(player) 
        }
    }

    // --- INTEGRATION: Get Warps from AxPlayerWarps ---
    fun getWarps(): List<WarpModel> {
        // Map AxPlayerWarps objects to your WarpModel objects so your GUI works
        return axApi.warpManager.warps.map { axWarp ->
            WarpModel(
                uuid = UUID.randomUUID(), // AxWarps might not expose a unique ID, or use axWarp.getId() if available
                icon = axWarp.icon ?: Material.ENDER_PEARL, // Use warp icon or fallback
                name = axWarp.name,
                x = axWarp.location.x,
                y = axWarp.location.y,
                z = axWarp.location.z,
                yaw = axWarp.location.yaw,
                pitch = axWarp.location.pitch,
                world = axWarp.location.world?.name ?: "world"
            )
        }
    }

    fun canTeleport(player: Player): Long {
        val lastTeleport = cooldownRepository.getPlayerLastWarpTime(player)
        return (lastTeleport + (warpsConfigRepository.cooldown() * 1000)) - Date().time
    }

    fun updateWarp(warpModel: WarpModel?) {
        updatingWarp = warpModel
    }

    fun getUpdateBuilder(): WarpModel.Builder? {
        val update = updatingWarp ?: return null
        return WarpModel.Builder()
            .name(update.name)
            .icon(update.icon)
            .uuid(update.uuid)
            .location(
                Location(
                    Bukkit.getWorld(update.world),
                    update.x,
                    update.y,
                    update.z,
                    update.yaw,
                    update.pitch,
                )
            )
    }

    // --- INTEGRATION: Create/Save to AxPlayerWarps ---
    fun saveWarp(warpModel: WarpModel) {
        // We override this to talk to the API instead of saving to JSON
        
        // 1. Check if warp exists
        val existingWarp = axApi.warpManager.getWarp(warpModel.name)
        
        if (existingWarp != null) {
            // Update logic (Depends on API version, usually you remove and re-add or setLocation)
            existingWarp.location = Location(
                Bukkit.getWorld(warpModel.world),
                warpModel.x, warpModel.y, warpModel.z, warpModel.yaw, warpModel.pitch
            )
            // If the API supports setting icons:
            existingWarp.icon = warpModel.icon
        } else {
            // Create new Warp
            axApi.warpManager.createWarp(
                warpModel.name,
                Location(Bukkit.getWorld(warpModel.world), warpModel.x, warpModel.y, warpModel.z, warpModel.yaw, warpModel.pitch),
                warpModel.uuid // Using UUID as owner, or specific UUID based on your logic
            )
        }
    }

    // --- INTEGRATION: Delete from AxPlayerWarps ---
    fun deleteWarp(warpModel: WarpModel) {
        val warp = axApi.warpManager.getWarp(warpModel.name)
        warp?.let {
            axApi.warpManager.removeWarp(it)
        }
    }
}

// Keep these data classes as they are required by your App/GUI system
data class ServerWarpsModel(
    var warps: List<WarpModel>,
) : ConfigModel()

data class WarpModel(
    val uuid: UUID,
    val icon: Material,
    val name: String,
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float,
    val pitch: Float,
    val world: String,
) {
    class Builder {
        var icon: Material? = null
        var name: String? = null
        var location: Location? = null
        var uuid: UUID? = null

        fun icon(icon: Material?): Builder {
            this.icon = icon
            return this
        }

        fun name(name: String): Builder {
            this.name = name
            return this
        }

        fun location(location: Location): Builder {
            this.location = location
            return this
        }

        fun uuid(uuid: UUID): Builder {
            this.uuid = uuid
            return this
        }

        fun build(): WarpModel? {
            return icon?.let { icon ->
                name?.let { name ->
                    location?.let { location ->
                        return WarpModel(
                            uuid = uuid ?: UUID.randomUUID(),
                            icon = icon,
                            name = name,
                            x = location.x,
                            y = location.y,
                            z = location.z,
                            yaw = location.yaw,
                            pitch = location.pitch,
                            world = location.world?.name ?: "null",
                        )
                    }
                }
            }
        }
    }
}
