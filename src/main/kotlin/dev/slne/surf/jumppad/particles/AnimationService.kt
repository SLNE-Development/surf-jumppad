package dev.slne.surf.jumppad.particles

import dev.slne.surf.api.paper.region.TickThreadGuard
import dev.slne.surf.jumppad.pad.JumpPadType
import org.bukkit.entity.Player
import kotlin.math.cos
import kotlin.math.sin

/**
 * Handles particle animations for jump pads.
 *
 * This service is responsible for spawning the start and boost particle effects
 * around players when they interact with a jump pad.
 */
@Suppress("UnstableApiUsage")
object AnimationService {
    /**
     * Number of cached positions used for the circular boost animation.
     */
    private const val BOOST_STEPS = 64

    /**
     * Radius of the circular boost particle path around the player.
     */
    private const val BOOST_RADIUS = 0.6

    /**
     * Vertical offset applied to boost particles relative to the player's position.
     */
    private const val BOOST_Y_OFFSET = 1.0

    /**
     * Precomputed X offsets for the circular boost particle path.
     */
    private val boostX = DoubleArray(BOOST_STEPS)

    /**
     * Precomputed Z offsets for the circular boost particle path.
     */
    private val boostZ = DoubleArray(BOOST_STEPS)

    init {
        for (i in 0 until BOOST_STEPS) {
            val angle = i * (2.0 * Math.PI / BOOST_STEPS)
            boostX[i] = BOOST_RADIUS * cos(angle)
            boostZ[i] = BOOST_RADIUS * sin(angle)
        }
    }

    /**
     * Plays the initial particle animation for a jump pad.
     *
     * The animation is spawned slightly above the player's current location and
     * uses the regular particle effect configured by the given [type].
     *
     * @param player the player at whose location the animation should be played
     * @param type the jump pad type that defines the particle effect
     */
    fun playStartAnimation(player: Player, type: JumpPadType) {
        TickThreadGuard.ensureTickThread(player, "Cannot play start animation on non-tick thread!")
        val loc = player.location.add(0.0, 1.0, 0.0)

        player.world.spawnParticle(
            type.particleEffect,
            loc,
            25,
            0.5, 0.0, 0.5,
            0.1
        )
    }

    /**
     * Plays a single step of the boost particle animation for a jump pad.
     *
     * The boost animation places one particle on a circular path around the player.
     * The circle positions are precomputed and selected based on the given [tick],
     * avoiding repeated trigonometric calculations during gameplay.
     *
     * @param player the player around whom the boost particle should be spawned
     * @param padType the jump pad type that defines the boost particle effect
     * @param tick the current animation tick used to select the circular particle position
     */
    fun playBoostAnimation(player: Player, padType: JumpPadType, tick: Int) {
        TickThreadGuard.ensureTickThread(player, "Cannot play boost animation on non-tick thread!")

        val base = player.location
        val index = Math.floorMod(tick, BOOST_STEPS)

        player.world.spawnParticle(
            padType.particleBoostEffect,
            base.x + boostX[index],
            base.y + BOOST_Y_OFFSET,
            base.z + boostZ[index],
            1,
            0.0, 0.0, 0.0,
            0.25
        )
    }
}