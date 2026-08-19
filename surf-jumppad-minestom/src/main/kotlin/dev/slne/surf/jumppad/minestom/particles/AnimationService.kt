package dev.slne.surf.jumppad.minestom.particles

import dev.slne.minestom.lobby.api.extension.getOrThrow
import dev.slne.minestom.lobby.api.extension.particleRegistry
import dev.slne.surf.jumppad.core.client.pad.JumpPadType
import dev.slne.surf.jumppad.core.client.pad.effect.JumpPadAnimation
import net.kyori.adventure.key.Key
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import net.minestom.server.network.packet.server.play.ParticlePacket
import net.minestom.server.particle.Particle
import net.minestom.server.registry.BuiltinRegistries
import net.minestom.server.registry.Registries
import java.util.concurrent.ConcurrentHashMap

/**
 * Handles particle animations for jump pads.
 *
 * This service is responsible for spawning the start and boost particle effects
 * around players when they interact with a jump pad.
 */
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
        val position = player.position

        player.sendPacketToViewersAndSelf(
            ParticlePacket(
                particleRegistry.getOrThrow(type.particleEffect),
                position.x(),
                position.y() + JumpPadAnimation.START_Y_OFFSET,
                position.z(),
                JumpPadAnimation.START_PARTICLE_SPREAD.toFloat(),
                0f,
                JumpPadAnimation.START_PARTICLE_SPREAD.toFloat(),
                JumpPadAnimation.START_PARTICLE_SPEED.toFloat(),
                JumpPadAnimation.START_PARTICLE_COUNT
            )
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
        val position = player.position

        player.sendPacketToViewersAndSelf(
            ParticlePacket(
                particleRegistry.getOrThrow(padType.particleEffect),
                position.x() + JumpPadAnimation.boostOffsetX(tick),
                position.y() + JumpPadAnimation.BOOST_Y_OFFSET,
                position.z() + JumpPadAnimation.boostOffsetZ(tick),
                0f, 0f, 0f,
                JumpPadAnimation.BOOST_PARTICLE_SPEED.toFloat(),
                1
            )
        )
    }
}
