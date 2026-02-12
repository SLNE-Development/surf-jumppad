package dev.slne.surf.jumppad.pad.service

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.github.shynixn.mccoroutine.folia.ticks
import dev.slne.surf.jumppad.plugin
import kotlinx.coroutines.*
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import kotlin.math.PI
import kotlin.math.sin

val jumpPadBoostService = JumpPadBoostService

object JumpPadBoostService {
    private val activeBoosts = mutableMapOf<Player, Job>()

    fun startBoost(
        player: Player,
        targetDist: Double,
        direction: Vector,
        peakHeight: Double
    ) {
        activeBoosts[player]?.cancel()

        val job = plugin.launch {
            withContext(plugin.entityDispatcher(player)) {
                runBoost(player, targetDist, direction, peakHeight)
            }
        }

        activeBoosts[player] = job
    }

    private suspend fun runBoost(
        player: Player,
        targetDist: Double,
        direction: Vector,
        initialPeak: Double
    ) {
        var peak = initialPeak
        val startLoc = player.location.clone()

        while (peak > 1.0) {
            if (collides(startLoc, direction, targetDist, peak)) {
                peak *= 0.8
            } else break
        }

        val totalTicks = (targetDist * 1.5 + 12).toInt().coerceIn(15, 80)

        var tick = 0
        while (currentCoroutineContext().isActive && tick < totalTicks && player.isOnline) {

            val progress = tick.toDouble() / totalTicks
            val nextProgress = (tick + 1).toDouble() / totalTicks

            val hPos = targetDist * progress
            val nextHPos = targetDist * nextProgress

            val yPos = sin(progress * PI) * peak + (progress * 0.2)
            val nextYPos = sin(nextProgress * PI) * peak + (nextProgress * 0.2)

            val currentVec = direction.clone().multiply(hPos).setY(yPos)
            val nextVec = direction.clone().multiply(nextHPos).setY(nextYPos)

            val velocity = nextVec.subtract(currentVec)

            player.velocity = velocity
            player.fallDistance = 0f

            tick++
            delay(1.ticks)
        }
    }

    private fun collides(
        start: Location,
        direction: Vector,
        targetDist: Double,
        peak: Double
    ): Boolean {
        val precision = 0.3
        val steps = (targetDist / precision).toInt()

        for (i in 0..steps) {
            val progress = i.toDouble() / steps
            val hDist = targetDist * progress
            val vDist = sin(progress * PI) * peak

            val checkLoc = start.clone()
                .add(direction.clone().multiply(hDist))
                .add(0.0, vDist + 1.8, 0.0)

            if (checkLoc.block.type != Material.AIR) return true
        }
        return false
    }
}