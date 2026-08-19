package dev.slne.surf.jumppad.core.client.pad.boost

import dev.slne.surf.jumppad.core.client.pad.JumpPadType

/**
 * The player a boost is applied to, as the boost itself needs to see them.
 *
 * Implementations adapt a platform's player and world to the few operations a boost performs, so
 * that the boost can run the same way everywhere while each platform keeps its own threading and
 * effect handling.
 */
interface JumpPadBoostTarget {
    /**
     * The x coordinate the player is currently at.
     */
    val positionX: Double

    /**
     * The y coordinate the player is currently at.
     */
    val positionY: Double

    /**
     * The z coordinate the player is currently at.
     */
    val positionZ: Double

    /**
     * Whether the player can still be boosted, meaning they are present and in a game mode that is
     * affected by movement.
     */
    val isBoostable: Boolean

    /**
     * Moves the player with the given velocity, in blocks per tick.
     *
     * @param x the velocity on the x axis
     * @param y the velocity on the y axis
     * @param z the velocity on the z axis
     */
    fun applyVelocity(x: Double, y: Double, z: Double)

    /**
     * Clears the distance the player has fallen so far, if the platform tracks it.
     */
    fun resetFallDistance()

    /**
     * Plays a single step of the boost animation.
     *
     * @param type the jump pad type defining the effects
     * @param tick the current animation tick
     */
    fun playBoostAnimation(type: JumpPadType, tick: Int)

    /**
     * Returns whether the block at the given block coordinates blocks movement.
     *
     * @param x the x coordinate of the block
     * @param y the y coordinate of the block
     * @param z the z coordinate of the block
     * @return `true` if the block is solid
     */
    fun isSolidBlockAt(x: Int, y: Int, z: Int): Boolean
}
