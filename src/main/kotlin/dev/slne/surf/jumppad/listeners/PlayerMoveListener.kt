package dev.slne.surf.jumppad.listeners

import dev.slne.surf.jumppad.pad.JumpPadType
import dev.slne.surf.jumppad.pad.jumpPadService
import org.bukkit.Particle
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.util.Vector
import java.util.*
import kotlin.time.Duration.Companion.seconds

object PlayerMoveListener : Listener {

    private val cooldowns = mutableMapOf<UUID, Long>()
    private val COOLDOWN = 3.seconds.inWholeMilliseconds

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        if (!event.hasExplicitlyChangedBlock()) return

        val player = event.player
        val to = event.to
        val pad = jumpPadService.getPadAt(to) ?: return

        val now = System.currentTimeMillis()
        val lastUse = cooldowns[player.uniqueId] ?: 0L

        if (now - lastUse < COOLDOWN) return

        val direction = player.eyeLocation.direction.clone().normalize()
        val loc = player.location
        val strength = pad.strength

        val boost: Vector = when (pad.type) {

            JumpPadType.VERTICAL -> Vector(0.0, strength, 0.0)

            JumpPadType.HORIZONTAL -> {
                direction.y = 0.1
                direction.multiply(strength)
            }
        }

        player.velocity = boost

        loc.world.spawnParticle(Particle.EXPLOSION, loc, 10)

        cooldowns[player.uniqueId] = now
    }
}
