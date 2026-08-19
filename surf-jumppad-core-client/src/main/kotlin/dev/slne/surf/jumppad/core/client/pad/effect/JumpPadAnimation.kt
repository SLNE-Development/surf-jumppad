package dev.slne.surf.jumppad.core.client.pad.effect

import kotlin.math.cos
import kotlin.math.sin

/**
 * The shape of the particle animations a jump pad plays.
 *
 * The circular boost path is precomputed once, so that spawning a particle during a boost does not
 * need any trigonometry.
 */
object JumpPadAnimation {
    /**
     * Number of cached positions used for the circular boost animation.
     */
    const val BOOST_STEPS = 64

    /**
     * Radius of the circular boost particle path around the player.
     */
    const val BOOST_RADIUS = 0.6

    /**
     * Vertical offset applied to boost particles relative to the player's position.
     */
    const val BOOST_Y_OFFSET = 1.0

    /**
     * Speed applied to a boost particle.
     */
    const val BOOST_PARTICLE_SPEED = 0.25

    /**
     * Vertical offset applied to the start particles relative to the player's position.
     */
    const val START_Y_OFFSET = 1.0

    /**
     * Number of particles spawned when a jump pad is triggered.
     */
    const val START_PARTICLE_COUNT = 25

    /**
     * Horizontal spread of the particles spawned when a jump pad is triggered.
     */
    const val START_PARTICLE_SPREAD = 0.5

    /**
     * Speed applied to a start particle.
     */
    const val START_PARTICLE_SPEED = 0.1

    private val boostX = DoubleArray(BOOST_STEPS)
    private val boostZ = DoubleArray(BOOST_STEPS)

    init {
        for (i in 0 until BOOST_STEPS) {
            val angle = i * (2.0 * Math.PI / BOOST_STEPS)
            boostX[i] = BOOST_RADIUS * cos(angle)
            boostZ[i] = BOOST_RADIUS * sin(angle)
        }
    }

    /**
     * Returns the x offset of the boost particle spawned on [tick].
     *
     * @param tick the current animation tick
     * @return the offset from the player's position on the x axis
     */
    fun boostOffsetX(tick: Int): Double = boostX[stepOf(tick)]

    /**
     * Returns the z offset of the boost particle spawned on [tick].
     *
     * @param tick the current animation tick
     * @return the offset from the player's position on the z axis
     */
    fun boostOffsetZ(tick: Int): Double = boostZ[stepOf(tick)]

    private fun stepOf(tick: Int) = tick and (BOOST_STEPS - 1)
}
