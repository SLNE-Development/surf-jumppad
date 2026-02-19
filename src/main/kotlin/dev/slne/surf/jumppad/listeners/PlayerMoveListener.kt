package dev.slne.surf.jumppad.listeners

import dev.slne.surf.jumppad.pad.JumpPadType
import dev.slne.surf.jumppad.pad.service.jumpPadBoostService
import dev.slne.surf.jumppad.pad.service.jumpPadService
import dev.slne.surf.jumppad.particles.animationService
import dev.slne.surf.jumppad.sounds.soundService
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.util.Vector
import java.util.*
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.seconds

object PlayerMoveListener : Listener {
    private val cooldowns: MutableMap<UUID, Long> = mutableMapOf()
    private val cooldown: Long = 3.seconds.inWholeMilliseconds

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        if (!event.hasExplicitlyChangedBlock()) return
        val player = event.player

        val pad = jumpPadService.getPadAt(event.to) ?: return

        val now = System.currentTimeMillis()
        val lastUse = cooldowns[player.uniqueId] ?: 0L
        if (now - lastUse < cooldown) return
        cooldowns[player.uniqueId] = now

        val startLoc = pad.origin.clone().add(0.5, 0.0, 0.5)

        val rawTargetLoc: Location = when (pad.type) {
            JumpPadType.STATIC -> {
                pad.targetLocation ?: pad.origin.clone().add(0.0, 5.0, 0.0)
            }

            JumpPadType.VERTICAL -> {
                val height = pad.distance.toDouble()
                player.velocity = Vector(0.0, sqrt(2 * 0.08 * height), 0.0)
                animationService.playStartAnimation(player, pad.type)
                soundService.playSound(player, pad.type)
                return
            }

            else -> {
                val dir = pad.type.getDirection(player)
                pad.origin.clone().add(dir.multiply(pad.distance.toDouble()))
            }
        }

        val targetLoc = rawTargetLoc.centerXZIfBlockAligned()

        val actualDistance = startLoc.distance(targetLoc)
        val peak = (actualDistance / 3.0).coerceAtLeast(3.0)

        jumpPadBoostService.startBoost(
            player = player,
            start = startLoc,
            target = targetLoc,
            peakHeight = peak,
            padType = pad.type
        )

        animationService.playStartAnimation(player, pad.type)
        soundService.playSound(player, pad.type)
    }

    private fun Location.centerXZIfBlockAligned(eps: Double = 1e-6): Location {
        val loc = this.clone()
        val bx = loc.blockX.toDouble()
        val bz = loc.blockZ.toDouble()

        if (abs(loc.x - bx) < eps) loc.x = bx + 0.5
        if (abs(loc.z - bz) < eps) loc.z = bz + 0.5

        return loc
    }
}