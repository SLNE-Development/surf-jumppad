package dev.slne.surf.jumppad.pad

import org.bukkit.Location
import java.util.UUID

data class JumpPad(
    val uuid : UUID,
    val origin: Location,
    val strength: Double,
    val width: Int,
    val length: Int,
    val type: JumpPadType
)