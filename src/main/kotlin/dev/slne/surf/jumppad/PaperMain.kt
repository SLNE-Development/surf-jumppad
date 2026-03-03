package dev.slne.surf.jumppad

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.jumppad.commands.jumpPadCommand
import dev.slne.surf.jumppad.listeners.PlayerInteractListener
import dev.slne.surf.jumppad.listeners.PlayerMoveListener
import dev.slne.surf.jumppad.listeners.PlayerTeleportListener
import dev.slne.surf.jumppad.pad.service.JumpPadService
import dev.slne.surf.surfapi.bukkit.api.event.register
import dev.slne.surf.surfapi.core.api.messages.builder.SurfComponentBuilder
import org.bukkit.Location
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

class PaperMain : SuspendingJavaPlugin() {

    override suspend fun onEnableAsync() {
        jumpPadCommand()

        PlayerMoveListener.register()
        PlayerInteractListener.register()
        PlayerTeleportListener.register()

        JumpPadService.registerPads()
    }

    override fun onDisable() {
    }
}

fun Location.formatToCoordString(): String {
    return String.format(Locale.US, "%.2f %.2f %.2f", x, y, z)
}

fun SurfComponentBuilder.appendBullet() {
    spacer("-")
    appendSpace()
}

val plugin get() = JavaPlugin.getPlugin(PaperMain::class.java)