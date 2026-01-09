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
        
        // Minecraft physics constants
        // In Minecraft, each tick: velocity = (velocity - gravity) * drag
        val gravity = 0.08
        val drag = 0.98
        
        // Use strength parameter to control the speed/time of flight
        // Higher strength = faster/shorter flight time
        val desiredSpeed = pad.strength.coerceAtLeast(0.5)
        
        // Calculate horizontal distance
        val horizontalDistance = kotlin.math.sqrt(dx * dx + dz * dz)
        
        // Estimate flight time: we want to reach the target in reasonable time
        // Start with an estimate based on desired speed
        var bestVelocityX = 0.0
        var bestVelocityY = 0.0
        var bestVelocityZ = 0.0
        var bestError = Double.MAX_VALUE
        
        // Try different flight times to find the best trajectory
        val minTime = (horizontalDistance / (desiredSpeed * 2.0)).coerceAtLeast(10.0)
        val maxTime = (horizontalDistance / (desiredSpeed * 0.5)).coerceAtMost(200.0)
        
        for (estimatedTicks in minTime.toInt()..maxTime.toInt() step 5) {
            // Simulate trajectory to find required initial velocities
            var testVelX = dx / estimatedTicks
            var testVelY = dy / estimatedTicks + gravity * estimatedTicks * 0.5
            var testVelZ = dz / estimatedTicks
            
            // Iterate to refine the velocities
            for (iteration in 0..20) {
                var simX = 0.0
                var simY = 0.0
                var simZ = 0.0
                var velX = testVelX
                var velY = testVelY
                var velZ = testVelZ
                
                // Simulate the trajectory
                for (tick in 0 until estimatedTicks) {
                    simX += velX
                    simY += velY
                    simZ += velZ
                    
                    // Apply Minecraft physics: (velocity - gravity) * drag
                    velX *= drag
                    velY = (velY - gravity) * drag
                    velZ *= drag
                }
                
                // Calculate error
                val errorX = dx - simX
                val errorY = dy - simY
                val errorZ = dz - simZ
                val totalError = kotlin.math.sqrt(errorX * errorX + errorY * errorY + errorZ * errorZ)
                
                // Check if this is the best solution so far
                if (totalError < bestError) {
                    bestError = totalError
                    bestVelocityX = testVelX
                    bestVelocityY = testVelY
                    bestVelocityZ = testVelZ
                }
                
                // If we're close enough, stop iterating
                if (totalError < 0.1) break
                
                // Adjust velocities based on error
                val adjustmentFactor = 0.5
                testVelX += errorX / estimatedTicks * adjustmentFactor
                testVelY += errorY / estimatedTicks * adjustmentFactor
                testVelZ += errorZ / estimatedTicks * adjustmentFactor
            }
            
            // If we found a good enough solution, use it
            if (bestError < 0.5) break
        }
        
        return Vector(bestVelocityX, bestVelocityY, bestVelocityZ)
    }
}