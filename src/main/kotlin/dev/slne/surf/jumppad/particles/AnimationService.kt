package dev.slne.surf.jumppad.particles

import dev.slne.surf.jumppad.pad.JumpPadType
import org.bukkit.entity.Player

val animationService = AnimationService

object AnimationService {
    fun playStartAnimation(player: Player, type: JumpPadType) {
        val loc = player.location.clone().add(0.0, 1.0, 0.0)

        player.world.spawnParticle(
            type.particleEffect,
            loc,
            25,
            0.5, 0.0, 0.5,
            0.1
        )
    }

    fun playBoostAnimation(player: Player, padType: JumpPadType, tick: Int) {
        val loc = player.location.clone().add(0.0, 1.0, 0.0)
        val radius = 0.6
        val angle = (tick % 360) * 0.15

        val x = radius * kotlin.math.cos(angle)
        val z = radius * kotlin.math.sin(angle)

        player.world.spawnParticle(
            padType.particleBoostEffect,
            loc.clone().add(x, 0.0, z),
            1,
            0.0, 0.0, 0.0,
            0.25
        )
    }
}