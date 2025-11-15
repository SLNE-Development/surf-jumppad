package dev.slne.surf.jumppad.pad

import org.bukkit.Location
import java.util.UUID

data class JumpPad(
    val uuid : UUID,
    val origin: Location,
    val strength: Double,
    val size: Int,
    val type: JumpPadType
)