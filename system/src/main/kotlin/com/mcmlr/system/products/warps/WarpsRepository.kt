package com.mcmlr.system.products.warps

import com.artillexstudios.axplayerwarps.AxPlayerWarps
// REMOVE THIS: import com.artillexstudios.axplayerwarps.api.AxPlayerWarpsAPI 
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

    // FIX 1: Access the Main Plugin Class directly
    private val axPlugin: AxPlayerWarps by lazy {
        AxPlayerWarps.getInstance()
    }

    init {
        model = ServerWarpsModel(listOf())
    }

    fun teleport(player: Player, warpName: String) {
        // FIX 2: Use the plugin instance to get the manager
        // Note: Check if it is 'warpManager' or 'getWarpManager()'
        val warp = axPlugin.warpManager.getWarp(warpName)
        
        if (warp != null) {
            warp.teleport(player)
            cooldownRepository.addPlayerLastWarpTime(player)
        }
    }

    fun getWarps(): List<WarpModel> {
        // FIX 3: Update access here too
        return axPlugin.warpManager.warps.map { axWarp ->
            WarpModel(
                uuid = UUID.randomUUID(), 
                icon = axWarp.icon ?: Material.ENDER_PEARL,
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

    // ... keep your canTeleport, updateWarp, getUpdateBuilder ...
    // (They don't use the API, so they are fine)

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

    fun saveWarp(warpModel: WarpModel) {
        // FIX 4: Update access
        val existingWarp = axPlugin.warpManager.getWarp(warpModel.name)
        
        if (existingWarp != null) {
            existingWarp.location = Location(
                Bukkit.getWorld(warpModel.world),
                warpModel.x, warpModel.y, warpModel.z, warpModel.yaw, warpModel.pitch
            )
            existingWarp.icon = warpModel.icon
        } else {
            axPlugin.warpManager.createWarp(
                warpModel.name,
                Location(Bukkit.getWorld(warpModel.world), warpModel.x, warpModel.y, warpModel.z, warpModel.yaw, warpModel.pitch),
                warpModel.uuid
            )
        }
    }

    fun deleteWarp(warpModel: WarpModel) {
        // FIX 5: Update access
        val warp = axPlugin.warpManager.getWarp(warpModel.name)
        warp?.let {
            axPlugin.warpManager.removeWarp(it)
        }
    }
}

// ... Keep your data classes below ...
