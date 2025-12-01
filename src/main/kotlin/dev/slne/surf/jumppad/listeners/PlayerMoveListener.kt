package dev.slne.surf.jumppad.listeners

import dev.slne.surf.jumppad.pad.JumpPadType
import dev.slne.surf.jumppad.pad.jumpPadService
import dev.slne.surf.jumppad.particles.animationService
import dev.slne.surf.jumppad.sounds.soundService
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.util.Vector
import java.util.*
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

        val strength = pad.strength

        val velocity: Vector = when (pad.type) {
            JumpPadType.HORIZONTAL_EAST -> Vector(strength, 0.0, 0.0)
            JumpPadType.HORIZONTAL_WEST -> Vector(-strength, 0.0, 0.0)
            JumpPadType.HORIZONTAL_NORTH -> Vector(0.0, 0.0, -strength)
            JumpPadType.HORIZONTAL_SOUTH -> Vector(0.0, 0.0, strength)
            JumpPadType.VERTICAL -> Vector(0.0, strength, 0.0)
        }
        player.velocity = velocity
        animationService.playAnimation(player, pad.type)
        soundService.playSound(player, pad.type)
    }
}
