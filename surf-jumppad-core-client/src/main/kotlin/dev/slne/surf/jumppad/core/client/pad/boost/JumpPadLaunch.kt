package dev.slne.surf.jumppad.core.client.pad.boost

import dev.slne.surf.jumppad.core.client.pad.JumpPad
import dev.slne.surf.jumppad.core.client.pad.JumpPadPosition
import dev.slne.surf.jumppad.core.client.pad.JumpPadType
import org.spongepowered.math.vector.Vector3d
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Everything a jump pad decides before a player is launched: where the launch starts, where it
 * leads, and how high its arc is.
 */
object JumpPadLaunch {
    /**
     * The offset that moves a block corner to the center of that block.
     */
    const val BLOCK_CENTER_OFFSET = 0.5

    /**
     * The height a static jump pad launches to when it has no target configured.
     */
    const val DEFAULT_STATIC_TARGET_HEIGHT = 5.0

    /**
     * The arc height no launch drops below.
     */
    const val MIN_ARC_PEAK_HEIGHT = 3.0

    /**
     * The share of the launch distance that becomes the arc height.
     */
    const val ARC_DISTANCE_DIVISOR = 3.0

    /**
     * The tolerance within which a coordinate counts as aligned to a block.
     */
    const val BLOCK_ALIGNMENT_EPSILON = 1e-6

    /**
     * The downwards acceleration a player is subject to, in blocks per tick squared.
     */
    const val MINECRAFT_GRAVITY = 0.08

    /**
     * The share of the remaining distance that is applied as velocity each tick.
     */
    const val VELOCITY_FACTOR = 0.25

    /**
     * The squared distance below which the final hop onto the target is applied.
     */
    const val FINAL_SNAP_DISTANCE_SQUARED = 1.0

    /**
     * Returns the position a launch from [pad] starts at, centered on the pad's origin block.
     *
     * @param pad the jump pad being used
     * @return the start position of the launch
     */
    fun startPosition(pad: JumpPad): JumpPadPosition =
        pad.origin.add(BLOCK_CENTER_OFFSET, 0.0, BLOCK_CENTER_OFFSET)

    /**
     * Calculates the target location for a non-vertical jump pad.
     *
     * Static pads use their configured target location if present. Directional
     * pads use their type-specific direction and configured distance.
     *
     * @param pad the jump pad to calculate the target for
     * @param playerDirection the direction the player using the jump pad is looking at
     * @return the calculated target location
     */
    fun targetPosition(pad: JumpPad, playerDirection: Vector3d): JumpPadPosition {
        return when (pad.type) {
            JumpPadType.STATIC -> {
                pad.targetLocation ?: pad.origin.add(0.0, DEFAULT_STATIC_TARGET_HEIGHT, 0.0)
            }

            else -> {
                val direction = pad.type.getDirection(playerDirection)
                val offset = direction.mul(pad.distance.toDouble())

                pad.origin.add(offset.x(), offset.y(), offset.z())
            }
        }
    }

    /**
     * Centers the X and Z coordinates if they are aligned to block coordinates.
     *
     * This keeps explicitly block-aligned target locations centered on the block
     * while preserving custom decimal coordinates.
     *
     * @param position the position to center
     * @param eps the floating-point tolerance used for block alignment checks
     * @return an optionally centered position
     */
    fun centerXZIfBlockAligned(
        position: JumpPadPosition,
        eps: Double = BLOCK_ALIGNMENT_EPSILON
    ): JumpPadPosition {
        val blockX = position.blockX.toDouble()
        val blockZ = position.blockZ.toDouble()

        val centeredX = if (abs(position.x - blockX) < eps) blockX + BLOCK_CENTER_OFFSET
        else position.x
        val centeredZ = if (abs(position.z - blockZ) < eps) blockZ + BLOCK_CENTER_OFFSET
        else position.z

        if (centeredX == position.x && centeredZ == position.z) return position
        return position.copy(x = centeredX, z = centeredZ)
    }

    /**
     * Returns the arc height a launch over [distance] blocks uses.
     *
     * @param distance the distance the launch covers
     * @return the arc height of the launch
     */
    fun peakHeight(distance: Double): Double =
        (distance / ARC_DISTANCE_DIVISOR).coerceAtLeast(MIN_ARC_PEAK_HEIGHT)

    /**
     * Returns the upwards velocity needed to reach [height] blocks, in blocks per tick.
     *
     * @param height the height to reach
     * @return the upwards velocity of the launch
     */
    fun verticalLaunchVelocity(height: Double): Double = sqrt(2.0 * MINECRAFT_GRAVITY * height)

    /**
     * Returns the velocity moving a player from their current position towards [desired], in blocks
     * per tick.
     *
     * @param desired the position the player should be at
     * @param currentX the x coordinate the player is at
     * @param currentY the y coordinate the player is at
     * @param currentZ the z coordinate the player is at
     * @return the velocity to apply this tick
     */
    fun followVelocity(
        desired: Vector3d,
        currentX: Double,
        currentY: Double,
        currentZ: Double
    ): Vector3d = Vector3d(
        (desired.x() - currentX) * VELOCITY_FACTOR,
        (desired.y() - currentY) * VELOCITY_FACTOR,
        (desired.z() - currentZ) * VELOCITY_FACTOR
    )
}
