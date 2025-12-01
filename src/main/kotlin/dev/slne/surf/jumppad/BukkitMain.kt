package dev.slne.surf.jumppad

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.jumppad.commands.jumpPadCommand
import dev.slne.surf.jumppad.listeners.BlockBreakListener
import dev.slne.surf.jumppad.listeners.PlayerJoinListener
import dev.slne.surf.jumppad.listeners.PlayerMoveListener
import dev.slne.surf.jumppad.storage.storageService
import dev.slne.surf.surfapi.bukkit.api.event.register
import org.bukkit.plugin.java.JavaPlugin

class BukkitMain : SuspendingJavaPlugin() {

    override fun onEnable() {
        jumpPadCommand()
        PlayerMoveListener.register()
        PlayerJoinListener.register()
        BlockBreakListener.register()

        storageService.init()
        storageService.loadPads()
    }

    override fun onDisable() {
        storageService.savePads()
    }
}

val plugin get() = JavaPlugin.getPlugin(BukkitMain::class.java)