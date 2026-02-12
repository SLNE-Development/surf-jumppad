package dev.slne.surf.jumppad.particles

import dev.slne.surf.jumppad.pad.JumpPadType
import org.bukkit.entity.Player

val animationService = AnimationService

object AnimationService {
    fun playAnimation(player: Player, type: JumpPadType) {
        val loc = player.location.clone().add(0.0, 1.0, 0.0)
        val direction = type.getDirection(player)

        if (type != JumpPadType.VERTICAL) {
            val effectLoc = loc.clone().add(direction.clone().multiply(0.5))

            player.world.spawnParticle(
                type.particleEffect,
                effectLoc,
                15,
                direction.x * 0.3, 0.1, direction.z * 0.3,
                0.05
            )
        } else {
            player.world.spawnParticle(
                type.particleEffect,
                loc,
                25,
                0.3, 0.8, 0.3,
                0.1
            )
        }
    }
}