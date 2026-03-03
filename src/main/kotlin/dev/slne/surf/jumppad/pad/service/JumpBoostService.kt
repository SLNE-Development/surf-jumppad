package dev.slne.surf.jumppad.pad.service

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.github.shynixn.mccoroutine.folia.ticks
import dev.slne.surf.jumppad.pad.JumpPadType
import dev.slne.surf.jumppad.particles.AnimationService
import dev.slne.surf.jumppad.plugin
import kotlinx.coroutines.*
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.PI
import kotlin.math.sin

val jumpPadBoostService = JumpPadBoostService

object JumpPadBoostService {
    private val activeBoosts = ConcurrentHashMap<UUID, Job>()

    fun isBoosting(player: Player): Boolean {
        return activeBoosts[player.uniqueId]?.isActive == true
    }

    fun startBoost(
        player: Player,
        start: Location,
        target: Location,
        peakHeight: Double,
        padType: JumpPadType
    ) {
        if (start.world != target.world) return
        if (player.gameMode == GameMode.SPECTATOR) return

        activeBoosts[player.uniqueId]?.cancel()

        val diff = target.toVector().subtract(start.toVector())
        val horizontalDiff = Vector(diff.x, 0.0, diff.z)
        val totalDist = horizontalDiff.length()
        if (totalDist < 0.1) return

        val direction = horizontalDiff.normalize()
        val yOffset = diff.y

        val job = plugin.launch {
            withContext(plugin.entityDispatcher(player)) {
                try {
                    runBoost(player, start.clone(), target.clone(), direction, totalDist, yOffset, peakHeight, padType)
                } finally {
                    activeBoosts.remove(player.uniqueId)
                }
            }
        }

        activeBoosts[player.uniqueId] = job
    }

    private suspend fun runBoost(
        player: Player,
        startLoc: Location,
        targetLoc: Location,
        direction: Vector,
        targetDist: Double,
        yOffset: Double,
        initialPeak: Double,
        padType: JumpPadType
    ) {
        var peak = initialPeak

        while (peak > 1.0) {
            if (collides(startLoc, direction, targetDist, yOffset, peak)) {
                peak *= 0.8
            } else break
        }

        val totalTicks = (targetDist * 1.5 + 15).toInt().coerceIn(20, 100)

        var tick = 0
        while (
            currentCoroutineContext().isActive &&
            tick < totalTicks &&
            player.isOnline &&
            !player.isDead &&
            player.gameMode != GameMode.SPECTATOR
        ) {
            val nextProgress = (tick + 1).toDouble() / totalTicks

            val nextHPos = targetDist * nextProgress
            val nextVPos = (sin(nextProgress * PI) * peak) + (nextProgress * yOffset)

            val desiredNext = startLoc.clone()
                .add(direction.clone().multiply(nextHPos))
                .add(0.0, nextVPos, 0.0)

            val diff = desiredNext.toVector().subtract(player.location.toVector())

            player.velocity = diff.multiply(0.25)

            player.fallDistance = 0f

            AnimationService.playBoostAnimation(player, padType, tick)

            tick++
            delay(1.ticks)
        }

        if (player.isOnline && !player.isDead && player.gameMode != GameMode.SPECTATOR) {
            val remaining = targetLoc.toVector().subtract(player.location.toVector())
            if (remaining.lengthSquared() < 1.0) {
                player.velocity = remaining
                player.fallDistance = 0f
            }
        }
    }

    fun stopBoost(player: Player) {
        activeBoosts[player.uniqueId]?.cancel()
        activeBoosts.remove(player.uniqueId)
    }

    private fun collides(start: Location, dir: Vector, dist: Double, yOff: Double, peak: Double): Boolean {
        val steps = (dist * 2).toInt().coerceAtLeast(5)
        for (i in 1..steps) {
            val p = i.toDouble() / steps
            val h = dist * p
            val v = (sin(p * PI) * peak) + (p * yOff)

            val hVec = dir.clone().multiply(h)
            val headLoc = start.clone().add(hVec).add(0.0, v + 1.8, 0.0)
            val footLoc = start.clone().add(hVec).add(0.0, v, 0.0)

            if (headLoc.block.type.isSolid || footLoc.block.type.isSolid) return true
        }
        return false
    }
}