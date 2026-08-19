package dev.slne.surf.jumppad

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.api.paper.event.register
import dev.slne.surf.jumppad.commands.jumpPadCommand
import dev.slne.surf.jumppad.listeners.PlayerInteractListener
import dev.slne.surf.jumppad.listeners.PlayerMoveListener
import dev.slne.surf.jumppad.listeners.PlayerTeleportListener
import dev.slne.surf.jumppad.storage.StorageService
import org.bukkit.plugin.java.JavaPlugin

class BukkitMain : SuspendingJavaPlugin() {

    override suspend fun onEnableAsync() {
        jumpPadCommand()

        PlayerMoveListener.register()
        PlayerInteractListener.register()
        PlayerTeleportListener.register()

        StorageService.loadPadsFromFile()
    }

    override suspend fun onDisableAsync() {
        StorageService.savePads()
    }
}

val plugin get() = JavaPlugin.getPlugin(BukkitMain::class.java)