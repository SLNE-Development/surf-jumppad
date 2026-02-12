package dev.slne.surf.jumppad.pad

import org.bukkit.Location
import java.util.*

data class JumpPad(
    val uuid: UUID,
    val origin: Location,
    val type: JumpPadType,
    val distance: Int,
    val width: Int,
    val length: Int
)