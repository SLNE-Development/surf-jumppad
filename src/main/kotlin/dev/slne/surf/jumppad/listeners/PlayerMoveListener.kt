package dev.slne.surf.jumppad.listeners

import dev.slne.surf.jumppad.pad.JumpPadType
import dev.slne.surf.jumppad.pad.service.jumpPadBoostService
import dev.slne.surf.jumppad.pad.service.jumpPadService
import dev.slne.surf.jumppad.particles.animationService
import dev.slne.surf.jumppad.sounds.soundService
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import java.util.*
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.seconds

object PlayerMoveListener : Listener {
    private val cooldowns: MutableMap<UUID, Long> = mutableMapOf()
    private val cooldown: Long = 3.seconds.inWholeMilliseconds

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        if (!event.hasExplicitlyChangedBlock()) return

        val player = event.player
        if (player.gameMode == GameMode.SPECTATOR) return

        val pad = jumpPadService.getPadAt(event.to) ?: return

        val now = System.currentTimeMillis()
        val lastUse = cooldowns[player.uniqueId] ?: 0L
        if (now - lastUse < cooldown) return
        cooldowns[player.uniqueId] = now

        val direction = pad.type.getDirection(player)

        if (pad.type == JumpPadType.VERTICAL) {
            val height = pad.distance.toDouble()
            val verticalVelocity = sqrt(2 * 0.08 * height)

            player.velocity = direction.clone().multiply(verticalVelocity)

        } else {
            val peak = (pad.distance / 3.5).coerceAtLeast(2.0)
            jumpPadBoostService.startBoost(
                player = player,
                targetDist = pad.distance.toDouble(),
                direction = direction,
                peakHeight = peak
            )
        }

        animationService.playAnimation(player, pad.type)
        soundService.playSound(player, pad.type)
    }
}