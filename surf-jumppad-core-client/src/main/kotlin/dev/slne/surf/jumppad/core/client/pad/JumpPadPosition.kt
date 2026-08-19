package dev.slne.surf.jumppad.core.client.pad

import net.kyori.adventure.key.Key
import kotlin.math.floor
import kotlin.math.sqrt

/**
 * A position in a world, independent of any server platform.
 *
 * @property worldKey the key of the world this position belongs to
 * @property x the x coordinate
 * @property y the y coordinate
 * @property z the z coordinate
 * @property yaw the yaw this position is looking at
 * @property pitch the pitch this position is looking at
 */
data class JumpPadPosition(
    val worldKey: Key,
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float = 0f,
    val pitch: Float = 0f
) {
    /**
     * The x coordinate of the block this position is inside of.
     */
    val blockX get() = floor(x).toInt()

    /**
     * The y coordinate of the block this position is inside of.
     */
    val blockY get() = floor(y).toInt()

    /**
     * The z coordinate of the block this position is inside of.
     */
    val blockZ get() = floor(z).toInt()

    /**
     * Returns a copy of this position moved by the given offsets.
     *
     * @param x the offset on the x axis
     * @param y the offset on the y axis
     * @param z the offset on the z axis
     * @return the moved position
     */
    fun add(x: Double, y: Double, z: Double) = copy(x = this.x + x, y = this.y + y, z = this.z + z)

    /**
     * Returns the distance between this position and [other].
     *
     * @param other the position to measure to
     * @return the distance between both positions
     */
    fun distance(other: JumpPadPosition): Double {
        val deltaX = other.x - x
        val deltaY = other.y - y
        val deltaZ = other.z - z

        return sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ)
    }
}
