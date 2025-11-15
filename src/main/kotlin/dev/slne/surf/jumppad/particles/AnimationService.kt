package dev.slne.surf.jumppad.particles

import org.bukkit.Particle
import org.bukkit.entity.Player

class AnimationService {
    fun showHorizontal(player: Player) {
        val loc = player.location.clone().subtract(player.location.direction.multiply(1.0))
        player.world.spawnParticle(
            Particle.SONIC_BOOM,
            loc,
            10,
            0.2, 0.2, 0.2,
            0.0
        )
    }

    fun showVertical(player: Player) {
        player.world.spawnParticle(
            Particle.EXPLOSION,
            player.location,
            20,
            0.5, 0.5, 0.5,
            0.1
        )
    }

    companion object {
        val INSTANCE = AnimationService()
    }
}

val animationService get() = AnimationService.INSTANCE
