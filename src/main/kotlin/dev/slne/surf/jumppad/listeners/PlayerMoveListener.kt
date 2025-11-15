package dev.slne.surf.jumppad.listeners

import dev.slne.surf.jumppad.pad.JumpPadType
import dev.slne.surf.jumppad.pad.jumpPadService
import dev.slne.surf.jumppad.particles.animationService
import dev.slne.surf.jumppad.sounds.soundService
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.util.Vector
import java.util.*
import kotlin.time.Duration.Companion.seconds

object PlayerMoveListener : Listener {

    private val cooldowns: MutableMap<UUID, Long> = mutableMapOf()
    private val cooldownMillis: Long = 3.seconds.inWholeMilliseconds

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        if (!event.hasExplicitlyChangedBlock()) return
        val player = event.player

        if (player.gameMode == GameMode.SPECTATOR) return

        val pad = jumpPadService.getPadAt(event.to) ?: return

        val now = System.currentTimeMillis()
        val lastUse = cooldowns[player.uniqueId] ?: 0L
        if (now - lastUse < cooldownMillis) return

        val boost = calculateBoost(player, pad.type, pad.strength)
        player.velocity = boost

        when (pad.type) {
            JumpPadType.HORIZONTAL -> {
                soundService.playHorizontal(player)
                animationService.showHorizontal(player)
            }

            JumpPadType.VERTICAL -> {
                soundService.playVertical(player)
                animationService.showVertical(player)
            }
        }

        cooldowns[player.uniqueId] = now
    }


    private fun calculateBoost(player: Player, type: JumpPadType, strength: Double): Vector =
        when (type) {
            JumpPadType.VERTICAL -> Vector(0.0, strength, 0.0)

            JumpPadType.HORIZONTAL -> {
                val moveDir = player.velocity.clone()
                if (moveDir.lengthSquared() < 0.01) {
                    player.location.direction.clone()
                } else {
                    moveDir
                }.apply {
                    y = 0.1
                    normalize().multiply(strength)
                }
            }
        }
}
