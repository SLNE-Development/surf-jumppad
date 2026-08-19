package dev.slne.surf.jumppad.core.client.pad.boost

import dev.slne.surf.jumppad.core.client.pad.JumpPadPosition
import org.spongepowered.math.vector.Vector3d
import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * The curved flight path a boosted player follows.
 *
 * A path is described by the horizontal direction it travels along, the horizontal distance it
 * covers and the height difference between its start and its target. The arc height itself is not
 * part of the path, because it is resolved against the surrounding blocks before the boost starts.
 *
 * @property startX the x coordinate the path starts at
 * @property startY the y coordinate the path starts at
 * @property startZ the z coordinate the path starts at
 * @property directionX the x component of the normalized horizontal direction
 * @property directionZ the z component of the normalized horizontal direction
 * @property distance the horizontal distance covered by the path
 * @property yOffset the height difference between the start and the target
 */
class JumpPadBoostPath(
    val startX: Double,
    val startY: Double,
    val startZ: Double,
    val directionX: Double,
    val directionZ: Double,
    val distance: Double,
    val yOffset: Double
) {
    /**
     * The number of ticks a boost along this path lasts.
     */
    val totalTicks = (distance * 1.5 + 15).toInt().coerceIn(MIN_TICKS, MAX_TICKS)

    /**
     * Returns the position a player should be at after [progress] of the path has been covered.
     *
     * @param peak the arc height of the path
     * @param progress how much of the path has been covered, between `0.0` and `1.0`
     * @return the position on the path
     */
    fun positionAt(peak: Double, progress: Double): Vector3d {
        val horizontalPosition = distance * progress
        val verticalPosition = sin(progress * PI) * peak + progress * yOffset

        return Vector3d(
            startX + directionX * horizontalPosition,
            startY + verticalPosition,
            startZ + directionZ * horizontalPosition
        )
    }

    /**
     * Returns the highest arc height at or below [initialPeak] that does not fly the player through
     * solid blocks.
     *
     * The height is lowered step by step for as long as the arc collides and stays above
     * [MIN_PEAK_HEIGHT]. If no height clears the path, the lowest one that was tried is returned.
     *
     * @param initialPeak the arc height to start from
     * @param isSolid whether the block at the given block coordinates is solid
     * @return the resolved arc height
     */
    fun resolvePeak(initialPeak: Double, isSolid: (Int, Int, Int) -> Boolean): Double {
        val steps = (distance * 2).toInt().coerceAtLeast(MIN_COLLISION_STEPS)

        val blockX = IntArray(steps)
        val blockZ = IntArray(steps)
        val arcHeights = DoubleArray(steps)
        val baseHeights = DoubleArray(steps)

        for (index in 0 until steps) {
            val progress = (index + 1).toDouble() / steps
            val horizontalPosition = distance * progress

            blockX[index] = floor(startX + directionX * horizontalPosition).toInt()
            blockZ[index] = floor(startZ + directionZ * horizontalPosition).toInt()
            arcHeights[index] = sin(progress * PI)
            baseHeights[index] = startY + progress * yOffset
        }

        var peak = initialPeak
        while (peak > MIN_PEAK_HEIGHT) {
            if (!collides(steps, blockX, blockZ, arcHeights, baseHeights, peak, isSolid)) {
                break
            }

            peak *= PEAK_REDUCTION_FACTOR
        }

        return peak
    }

    private inline fun collides(
        steps: Int,
        blockX: IntArray,
        blockZ: IntArray,
        arcHeights: DoubleArray,
        baseHeights: DoubleArray,
        peak: Double,
        isSolid: (Int, Int, Int) -> Boolean
    ): Boolean {
        for (index in 0 until steps) {
            val footY = baseHeights[index] + arcHeights[index] * peak
            val x = blockX[index]
            val z = blockZ[index]

            if (isSolid(x, floor(footY).toInt(), z)) return true
            if (isSolid(x, floor(footY + PLAYER_COLLISION_HEIGHT).toInt(), z)) return true
        }

        return false
    }

    companion object {
        /**
         * The smallest horizontal distance a boost is started for.
         */
        const val MIN_HORIZONTAL_DISTANCE = 0.1

        /**
         * The arc height a boost is never lowered below.
         */
        const val MIN_PEAK_HEIGHT = 1.0

        /**
         * The factor the arc height is lowered by while it still collides.
         */
        const val PEAK_REDUCTION_FACTOR = 0.8

        /**
         * The height a player occupies above the position they stand on.
         */
        const val PLAYER_COLLISION_HEIGHT = 1.8

        private const val MIN_TICKS = 20
        private const val MAX_TICKS = 100
        private const val MIN_COLLISION_STEPS = 5

        /**
         * Builds the path leading from [start] to [target].
         *
         * @param start the position the path starts at
         * @param target the position the path leads to
         * @return the path, or `null` if both positions are too close together to boost between
         */
        fun between(start: JumpPadPosition, target: JumpPadPosition): JumpPadBoostPath? {
            val deltaX = target.x - start.x
            val deltaZ = target.z - start.z

            val distance = sqrt(deltaX * deltaX + deltaZ * deltaZ)
            if (distance < MIN_HORIZONTAL_DISTANCE) return null

            return JumpPadBoostPath(
                startX = start.x,
                startY = start.y,
                startZ = start.z,
                directionX = deltaX / distance,
                directionZ = deltaZ / distance,
                distance = distance,
                yOffset = target.y - start.y
            )
        }
    }
}
