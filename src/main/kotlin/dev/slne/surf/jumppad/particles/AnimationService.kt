package dev.slne.surf.jumppad.particles

import dev.slne.surf.jumppad.pad.JumpPadType
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class AnimationService {
    fun playAnimation(player: Player, type: JumpPadType) {
        when (type) {
            JumpPadType.HORIZONTAL_EAST -> showHorizontal(player, Vector(1, 0, 0))
            JumpPadType.HORIZONTAL_WEST -> showHorizontal(player, Vector(-1, 0, 0))
            JumpPadType.HORIZONTAL_NORTH -> showHorizontal(player, Vector(0, 0, -1))
            JumpPadType.HORIZONTAL_SOUTH -> showHorizontal(player, Vector(0, 0, 1))
            JumpPadType.VERTICAL -> showVertical(player)
        }
    }

    private fun showHorizontal(player: Player, direction: Vector) {
        val loc = player.location.clone().add(direction.multiply(1.0))
        player.world.spawnParticle(
            Particle.SONIC_BOOM,
            loc.x, loc.y, loc.z,
            10,
            direction.x * 0.2, direction.y * 0.2, direction.z * 0.2,
            0.0
        )
    }

    private fun showVertical(player: Player) {
        val loc = player.location.clone().add(0.0, 1.0, 0.0)
        player.world.spawnParticle(
            Particle.EXPLOSION,
            loc.x, loc.y, loc.z,
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