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
import java.lang.Math.pow
import java.util.*
import kotlin.math.pow
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
        
        // Minecraft physics constants
        val gravity = 0.08 // blocks/tick^2
        val drag = 0.98 // velocity multiplier per tick
        
        // Calculate horizontal distance
        val horizontalDistance = kotlin.math.sqrt(dx * dx + dz * dz)
        
        // Estimate flight time based on strength parameter
        // strength acts as a horizontal speed multiplier
        val estimatedHorizontalSpeed = pad.strength.coerceAtLeast(0.1)
        
        // Calculate flight time in ticks using iterative approach
        // We estimate the time needed to cover the horizontal distance
        var timeInTicks = horizontalDistance / estimatedHorizontalSpeed
        
        // Ensure minimum flight time to avoid division issues
        timeInTicks = timeInTicks.coerceAtLeast(1.0)
        
        // Calculate horizontal velocities
        // With drag, velocity at tick t = v0 * drag^t
        // Total distance = sum of (v0 * drag^t) for t=0 to timeInTicks
        // This approximates to: v0 * (1 - drag^time) / (1 - drag)
        val dragSum = if (drag < 1.0) {
            (1 - drag.pow(timeInTicks)) / (1 - drag)
        } else {
            timeInTicks // fallback if drag is 1.0
        }
        
        val velocityX = dx / dragSum
        val velocityZ = dz / dragSum
        
        // Calculate vertical velocity needed
        // With gravity and drag: position = sum of (v0 * drag^t - gravity * (t+1))
        // Simplified: we need to account for both drag on velocity and cumulative gravity
        var totalVerticalDisplacement = 0.0
        var currentVelocityY = 1.0 // placeholder, we'll solve for this
        
        // Iterate to find the correct initial vertical velocity
        // This accounts for drag reducing velocity and gravity accumulating
        for (attempt in 0..10) {
            totalVerticalDisplacement = 0.0
            currentVelocityY = dy / dragSum + gravity * timeInTicks * 0.5 // initial estimate
            
            var tempVelocityY = currentVelocityY
            for (tick in 0 until timeInTicks.toInt()) {
                totalVerticalDisplacement += tempVelocityY
                // Apply drag first, then subtract gravity (Minecraft physics order)
                tempVelocityY = tempVelocityY * drag - gravity
            }
            
            // Check if we're close enough
            if (kotlin.math.abs(totalVerticalDisplacement - dy) < 0.1) break
            
            // Adjust estimate based on error
            val error = dy - totalVerticalDisplacement
            currentVelocityY += error / timeInTicks
        }
        
        return Vector(velocityX, currentVelocityY, velocityZ)
    }
}