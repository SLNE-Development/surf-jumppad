package dev.slne.surf.jumppad.minestom

import com.google.auto.service.AutoService
import dev.slne.minestom.lobby.api.plugin.MinestomPlugin
import dev.slne.minestom.lobby.api.plugin.annotation.MinestomPluginMeta
import dev.slne.surf.jumppad.minestom.command.JumpPadCommandRegistrar
import dev.slne.surf.jumppad.minestom.listener.JumpPadListener

@AutoService(MinestomPlugin::class)
@MinestomPluginMeta(
    "surf-jumppad-minestom",
    dependsOn = ["surf-api-minestom"]
)
class SurfJumpPadMinestomPlugin : MinestomPlugin(SurfJumpPadMinestomEntrypoint::class.java) {
    override fun configurePlugin() {
        bindEventRegistrar<JumpPadListener>()
        bindCommandRegistrar<JumpPadCommandRegistrar>()
    }
}
