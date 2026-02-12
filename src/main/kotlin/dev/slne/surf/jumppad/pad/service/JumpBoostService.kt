package dev.slne.surf.jumppad.pad.service

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.github.shynixn.mccoroutine.folia.ticks
import dev.slne.surf.jumppad.plugin
import kotlinx.coroutines.*
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import kotlin.math.PI
import kotlin.math.sin

val jumpPadBoostService = JumpPadBoostService

object JumpPadBoostService {
    private val activeBoosts = mutableMapOf<Player, Job>()

    fun startBoost(
        player: Player,
        target: Location,
        peakHeight: Double
    ) {
        val start = player.location
        if (start.world != target.world) return
        if(player.gameMode == GameMode.SPECTATOR) return

        activeBoosts[player]?.cancel()

        val diff = target.toVector().subtract(start.toVector())
        val horizontalDiff = Vector(diff.x, 0.0, diff.z)
        val totalDist = horizontalDiff.length()

        if (totalDist < 0.1) return

        val direction = horizontalDiff.normalize()
        val yOffset = diff.y

        val job = plugin.launch {
            withContext(plugin.entityDispatcher(player)) {
                runBoost(player, start.clone(), direction, totalDist, yOffset, peakHeight)
            }
        }

        activeBoosts[player] = job
    }

    private suspend fun runBoost(
        player: Player,
        startLoc: Location,
        direction: Vector,
        targetDist: Double,
        yOffset: Double,
        initialPeak: Double
    ) {
        var peak = initialPeak

        while (peak > 1.0) {
            if (collides(startLoc, direction, targetDist, yOffset, peak)) {
                peak *= 0.8
            } else break
        }

        val totalTicks = (targetDist * 1.5 + 15).toInt().coerceIn(20, 100)

        var tick = 0
        while (currentCoroutineContext().isActive && tick < totalTicks && player.isOnline && !player.isDead && player.gameMode != GameMode.SPECTATOR) {
            val progress = tick.toDouble() / totalTicks
            val nextProgress = (tick + 1).toDouble() / totalTicks

            val hPos = targetDist * progress
            val vPos = (sin(progress * PI) * peak) + (progress * yOffset)

            val nextHPos = targetDist * nextProgress
            val nextVPos = (sin(nextProgress * PI) * peak) + (nextProgress * yOffset)

            val currentVec = direction.clone().multiply(hPos).setY(vPos)
            val nextVec = direction.clone().multiply(nextHPos).setY(nextVPos)

            player.velocity = nextVec.subtract(currentVec)
            player.fallDistance = 0f

            tick++
            delay(1.ticks)
        }
    }

    private fun collides(start: Location, dir: Vector, dist: Double, yOff: Double, peak: Double): Boolean {
        val steps = (dist * 2).toInt().coerceAtLeast(5)
        for (i in 1..steps) {
            val p = i.toDouble() / steps
            val h = dist * p
            val v = (sin(p * PI) * peak) + (p * yOff)

            val headLoc = start.clone().add(dir.clone().multiply(h)).add(0.0, v + 1.8, 0.0)
            val footLoc = start.clone().add(dir.clone().multiply(h)).add(0.0, v, 0.0)

            if (headLoc.block.type.isSolid || footLoc.block.type.isSolid) return true
        }
        return false
    }
}