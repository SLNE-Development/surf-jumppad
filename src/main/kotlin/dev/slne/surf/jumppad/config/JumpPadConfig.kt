package dev.slne.surf.jumppad.config

import dev.slne.surf.jumppad.pad.JumpPad
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class JumpPadConfig(
    val teleporters: MutableList<JumpPad>
)