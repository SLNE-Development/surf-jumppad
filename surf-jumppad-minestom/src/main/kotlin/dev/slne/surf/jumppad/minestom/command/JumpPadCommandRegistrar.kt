package dev.slne.surf.jumppad.minestom.command

import com.google.inject.Singleton
import dev.slne.minestom.lobby.api.command.CommandRegistrar

@Singleton
class JumpPadCommandRegistrar : CommandRegistrar {
    override fun register() {
        jumpPadCommand()
    }
}
