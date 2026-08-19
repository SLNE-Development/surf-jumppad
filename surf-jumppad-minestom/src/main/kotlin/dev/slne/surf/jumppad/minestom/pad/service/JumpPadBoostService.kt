package dev.slne.surf.jumppad.minestom.pad.service

import dev.slne.minestom.lobby.api.coroutine.MinestomDispatchers
import dev.slne.minestom.lobby.api.coroutine.minestomScope
import dev.slne.surf.jumppad.core.client.pad.JumpPadPosition
import dev.slne.surf.jumppad.core.client.pad.JumpPadType
import dev.slne.surf.jumppad.core.client.pad.boost.JumpPadBoostPath
import dev.slne.surf.jumppad.core.client.pad.boost.JumpPadBoostTarget
import dev.slne.surf.jumppad.core.client.pad.boost.runBoost
import dev.slne.surf.jumppad.minestom.pad.instance
import dev.slne.surf.jumppad.minestom.particles.AnimationService
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import net.minestom.server.ServerFlag
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player
import net.minestom.server.instance.Instance
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Handles active jump pad boost movements.
 *
 * Each player can only have one active boost at a time. Starting a new boost
 * cancels the previous one and replaces it with a new coroutine running on the
 * server tick thread.
 */
object JumpPadBoostService {
    private val activeBoosts = ConcurrentHashMap<UUID, Job>()

    /**
     * Checks whether the given player currently has an active boost.
     *
     * @param player the player to check
     * @return `true` if the player is currently boosted, otherwise `false`
     */
    fun isBoosting(player: Player): Boolean {
        return activeBoosts[player.uuid]?.isActive == true
    }

    /**
     * Starts a new boost for the given player.
     *
     * The boost follows a curved trajectory from [start] to [target]. The curve
     * height is controlled by [peakHeight] and may be reduced automatically if
     * the initial trajectory would collide with solid blocks.
     *
     * @param player the player to boost
     * @param start the start location of the boost
     * @param target the target location of the boost
     * @param peakHeight the initial peak height of the boost arc
     * @param padType the jump pad type used for boost particles
     */
    fun startBoost(
        player: Player,
        start: JumpPadPosition,
        target: JumpPadPosition,
        peakHeight: Double,
        padType: JumpPadType
    ) {
        val instance = start.instance() ?: return
        if (target.worldKey != start.worldKey) return
        if (player.gameMode == GameMode.SPECTATOR) return

        val path = JumpPadBoostPath.between(start, target) ?: return

        val playerUuid = player.uuid
        activeBoosts[playerUuid]?.cancel("New boost started")

        val boostTarget = PlayerBoostTarget(player, instance)

        var job: Job? = null
        job = minestomScope.launch(MinestomDispatchers.Main, CoroutineStart.LAZY) {
            try {
                runBoost(
                    target = boostTarget,
                    path = path,
                    targetPosition = target,
                    initialPeak = peakHeight,
                    padType = padType
                )
            } finally {
                job?.let { activeBoosts.remove(playerUuid, it) }
            }
        }

        activeBoosts[playerUuid] = job
        job.start()
    }

    /**
     * Stops the active boost of the given player, if one exists.
     *
     * @param player the player whose boost should be stopped
     */
    fun stopBoost(player: Player) {
        activeBoosts.remove(player.uuid)?.cancel("Boost stopped")
    }

    private class PlayerBoostTarget(
        private val player: Player,
        private val instance: Instance
    ) : JumpPadBoostTarget {
        override val positionX get() = player.position.x()
        override val positionY get() = player.position.y()
        override val positionZ get() = player.position.z()

        override val isBoostable
            get() = player.isOnline && !player.isDead && player.gameMode != GameMode.SPECTATOR

        override fun applyVelocity(x: Double, y: Double, z: Double) {
            player.velocity = Vec(
                x * ServerFlag.SERVER_TICKS_PER_SECOND,
                y * ServerFlag.SERVER_TICKS_PER_SECOND,
                z * ServerFlag.SERVER_TICKS_PER_SECOND
            )
        }

        override fun resetFallDistance() = Unit

        override fun playBoostAnimation(type: JumpPadType, tick: Int) {
            AnimationService.playBoostAnimation(player, type, tick)
        }

        override fun isSolidBlockAt(x: Int, y: Int, z: Int) = instance.getBlock(x, y, z).solid()
    }
}
