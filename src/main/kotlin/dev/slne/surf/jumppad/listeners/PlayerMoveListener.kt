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
        
        // Trajectory calculation parameters
        val minSpeedMultiplier = 0.5
        val maxSpeedMultiplier = 2.0
        val minFlightTime = 10.0
        val maxFlightTime = 200.0
        val timeStepSize = 5
        val maxIterations = 20
        val convergenceThreshold = 0.1
        val velocityAdjustmentFactor = 0.5
        val acceptableError = 0.5
        val initialVelocityEstimateFactor = 0.5
        
        // Use strength parameter to control the speed/time of flight
        // Higher strength = faster/shorter flight time
        val desiredSpeed = pad.strength.coerceAtLeast(minSpeedMultiplier)
        
        // Calculate horizontal and total distance
        val horizontalDistance = kotlin.math.sqrt(dx * dx + dz * dz)
        val totalDistance = kotlin.math.sqrt(dx * dx + dy * dy + dz * dz)
        
        // Estimate flight time: we want to reach the target in reasonable time
        // Start with an estimate based on desired speed
        var bestVelocityX = 0.0
        var bestVelocityY = 0.0
        var bestVelocityZ = 0.0
        var bestError = Double.MAX_VALUE
        
        // Try different flight times to find the best trajectory
        // Account for height differences - going up needs more time, going down needs adjustment
        val heightAdjustmentFactor = if (dy > 0) {
            // Going up: need more time to fight gravity
            1.0 + (kotlin.math.abs(dy) / (horizontalDistance.coerceAtLeast(1.0))) * 0.5
        } else if (dy < 0) {
            // Going down: gravity helps, but still need time
            1.0 + (kotlin.math.abs(dy) / (horizontalDistance.coerceAtLeast(1.0))) * 0.3
        } else {
            1.0
        }
        
        val baseMinTime = (totalDistance / (desiredSpeed * maxSpeedMultiplier * heightAdjustmentFactor)).coerceAtLeast(minFlightTime)
        val baseMaxTime = (totalDistance / (desiredSpeed * minSpeedMultiplier * heightAdjustmentFactor)).coerceAtMost(maxFlightTime)
        
        // Ensure minTime doesn't exceed maxTime
        val effectiveMinTime = baseMinTime.coerceAtMost(baseMaxTime)
        val effectiveMaxTime = baseMaxTime.coerceAtLeast(effectiveMinTime)
        
        for (estimatedTicks in effectiveMinTime.toInt()..effectiveMaxTime.toInt() step timeStepSize) {
            // Simulate trajectory to find required initial velocities
            // Better initial estimate considering height difference
            var testVelX = dx / estimatedTicks
            var testVelZ = dz / estimatedTicks
            
            // For vertical velocity, we need to account for gravity over the flight time
            // If going up, we need extra velocity to overcome gravity
            // If going down, gravity assists but we still need careful calculation
            val gravityEffect = gravity * estimatedTicks * initialVelocityEstimateFactor
            var testVelY = if (dy > 0) {
                // Going up: need more initial velocity
                dy / estimatedTicks + gravityEffect * 2.0
            } else if (dy < 0) {
                // Going down: gravity helps
                dy / estimatedTicks + gravityEffect * 0.5
            } else {
                // Level flight
                gravityEffect
            }
            
            // Iterate to refine the velocities
            for (iteration in 0..maxIterations) {
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
                if (totalError < convergenceThreshold) break
                
                // Adjust velocities based on error
                // Use a higher adjustment factor for vertical errors when dealing with height differences
                val verticalAdjustmentFactor = if (kotlin.math.abs(dy) > horizontalDistance * 0.5) {
                    velocityAdjustmentFactor * 1.5 // More aggressive for steep trajectories
                } else {
                    velocityAdjustmentFactor
                }
                
                testVelX += errorX / estimatedTicks * velocityAdjustmentFactor
                testVelY += errorY / estimatedTicks * verticalAdjustmentFactor
                testVelZ += errorZ / estimatedTicks * velocityAdjustmentFactor
            }
            
            // If we found a good enough solution, use it
            if (bestError < acceptableError) break
        }
        
        return Vector(bestVelocityX, bestVelocityY, bestVelocityZ)
    }
}