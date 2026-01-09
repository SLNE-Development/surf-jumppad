package dev.slne.surf.jumppad.listeners

import dev.slne.surf.jumppad.pad.JumpPad
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
            JumpPadType.STATIC -> calculateStaticVelocity(player.location, pad)
        }
        player.velocity = velocity
        cooldowns[player.uniqueId] = now
        animationService.playAnimation(player, pad.type)
        soundService.playSound(player, pad.type)
    }

    private fun calculateStaticVelocity(playerLocation: org.bukkit.Location, pad: JumpPad): Vector {
        val target = pad.target ?: return Vector(0.0, pad.strength, 0.0)
        
        // Calculate the difference between target and player position
        val dx = target.x - playerLocation.x
        val dy = target.y - playerLocation.y
        val dz = target.z - playerLocation.z
        
        // Minecraft gravity is 0.08 blocks/tick^2, with drag of 0.98 per tick
        // We use a simplified calculation for the trajectory
        val gravity = 0.08
        val drag = 0.98
        
        // Calculate horizontal distance
        val horizontalDistance = kotlin.math.sqrt(dx * dx + dz * dz)
        
        // Use the strength as a time factor to determine flight time
        // Higher strength = faster arrival
        val timeInTicks = if (pad.strength > 0) {
            horizontalDistance / pad.strength * 20 // Approximate time in ticks
        } else {
            20.0
        }
        
        // Calculate required velocities considering drag
        // For drag, we need to account for velocity reduction over time
        val dragFactor = (1 - kotlin.math.pow(drag, timeInTicks)) / (1 - drag)
        
        val velocityX = dx / dragFactor
        val velocityZ = dz / dragFactor
        
        // Calculate vertical velocity needed
        // Formula: dy = v_y * time - 0.5 * g * time^2 (accounting for drag)
        val velocityY = (dy + 0.5 * gravity * timeInTicks * timeInTicks) / dragFactor
        
        return Vector(velocityX, velocityY, velocityZ)
    }
}