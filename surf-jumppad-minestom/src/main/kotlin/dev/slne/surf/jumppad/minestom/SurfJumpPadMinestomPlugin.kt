package dev.slne.surf.jumppad.minestom

import dev.slne.surf.api.minestom.plugin.SurfMinestomPlugin
import dev.slne.surf.jumppad.minestom.command.jumpPadCommand
import dev.slne.surf.jumppad.minestom.listener.JumpPadListener
import dev.slne.surf.jumppad.minestom.storage.StorageService

object SurfJumpPadMinestomPlugin : SurfMinestomPlugin() {
    override suspend fun onLoad() {
        StorageService.loadPads()
    }

    override suspend fun onEnable() {
        jumpPadCommand()

        JumpPadListener.register(eventNode)
    }

    override suspend fun onDisable() {
        StorageService.savePads()
    }
}
