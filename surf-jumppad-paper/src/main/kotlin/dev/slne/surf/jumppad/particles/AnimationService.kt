package dev.slne.surf.jumppad.particles

import dev.slne.surf.api.paper.region.TickThreadGuard
import dev.slne.surf.jumppad.core.client.pad.JumpPadType
import dev.slne.surf.jumppad.core.client.pad.effect.JumpPadAnimation
import org.bukkit.Registry
import org.bukkit.entity.Player

/**
 * Handles particle animations for jump pads.
 *
 * This service is responsible for spawning the start and boost particle effects
 * around players when they interact with a jump pad.
 */
@Suppress("UnstableApiUsage")
object AnimationService {

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
        val loc = player.location.add(0.0, JumpPadAnimation.START_Y_OFFSET, 0.0)

        player.world.spawnParticle(
            Registry.PARTICLE_TYPE.getOrThrow(type.particleEffect),
            loc,
            JumpPadAnimation.START_PARTICLE_COUNT,
            JumpPadAnimation.START_PARTICLE_SPREAD,
            0.0,
            JumpPadAnimation.START_PARTICLE_SPREAD,
            JumpPadAnimation.START_PARTICLE_SPEED
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

        player.world.spawnParticle(
            Registry.PARTICLE_TYPE.getOrThrow(padType.particleBoostEffect),
            base.x + JumpPadAnimation.boostOffsetX(tick),
            base.y + JumpPadAnimation.BOOST_Y_OFFSET,
            base.z + JumpPadAnimation.boostOffsetZ(tick),
            1,
            0.0, 0.0, 0.0,
            JumpPadAnimation.BOOST_PARTICLE_SPEED
        )
    }
}