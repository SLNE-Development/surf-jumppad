package dev.slne.surf.jumppad

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.jumppad.commands.jumpPadCommand
import dev.slne.surf.jumppad.listeners.PlayerMoveEvent
import dev.slne.surf.surfapi.bukkit.api.event.register
import org.bukkit.plugin.java.JavaPlugin
import kotlin.jvm.java

class SurfJumpPad : SuspendingJavaPlugin() {

    override fun onEnable() {
        jumpPadCommand()
        PlayerMoveEvent.register()
        // Plugin startup logic
    }

    override suspend fun onDisableAsync() {
    // Plugin shutdown logic
    }
}
val plugin get() = JavaPlugin.getPlugin(SurfJumpPad::class.java)