package dev.slne.surf.jumppad.pad

import org.bukkit.Location
import java.util.*

/**
 * Represents a jump pad placed in the world.
 *
 * A jump pad has an origin location, a type, a launch distance, and a rectangular
 * trigger area defined by its width and length.
 *
 * @property uuid the unique identifier of this jump pad
 * @property origin the origin location of the jump pad
 * @property type the type of jump pad and its configured behavior
 * @property distance the launch distance used by this jump pad
 * @property targetLocation the optional target location this jump pad should launch to
 * @property width the width of the jump pad area on the X axis
 * @property length the length of the jump pad area on the Z axis
 */
data class JumpPad(
    val uuid: UUID,
    val origin: Location,
    val type: JumpPadType,
    val distance: Int,
    val targetLocation: Location? = null,
    val width: Int,
    val length: Int
)