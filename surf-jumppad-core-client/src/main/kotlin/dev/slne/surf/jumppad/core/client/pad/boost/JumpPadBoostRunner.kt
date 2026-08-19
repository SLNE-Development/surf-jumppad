package dev.slne.surf.jumppad.core.client.pad.boost

import dev.slne.surf.jumppad.core.client.pad.JumpPadPosition
import dev.slne.surf.jumppad.core.client.pad.JumpPadType
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * The delay that advances a boost by one server tick.
 */
val BOOST_TICK_DELAY: Duration = 25.milliseconds

/**
 * Flies a player along a jump pad's trajectory.
 *
 * The boost nudges the player towards the position they should be at each tick rather than
 * teleporting them, so that the movement stays smooth on the client. It ends once the path has been
 * covered, once the player can no longer be boosted, or once the surrounding coroutine is
 * cancelled.
 *
 * @param target the player being boosted
 * @param path the trajectory to follow
 * @param targetPosition the position the path leads to
 * @param initialPeak the arc height to attempt before it is resolved against the world
 * @param padType the jump pad type defining the boost effects
 */
suspend fun runBoost(
    target: JumpPadBoostTarget,
    path: JumpPadBoostPath,
    targetPosition: JumpPadPosition,
    initialPeak: Double,
    padType: JumpPadType
) {
    val peak = path.resolvePeak(initialPeak, target::isSolidBlockAt)
    val totalTicks = path.totalTicks

    var tick = 0
    while (currentCoroutineContext().isActive && tick < totalTicks && target.isBoostable) {
        val progress = (tick + 1).toDouble() / totalTicks
        val desiredPosition = path.positionAt(peak, progress)

        val velocity = JumpPadLaunch.followVelocity(
            desired = desiredPosition,
            currentX = target.positionX,
            currentY = target.positionY,
            currentZ = target.positionZ
        )

        target.applyVelocity(velocity.x(), velocity.y(), velocity.z())
        target.resetFallDistance()

        target.playBoostAnimation(padType, tick)

        tick++
        delay(BOOST_TICK_DELAY)
    }

    if (!target.isBoostable) return

    val remainingX = targetPosition.x - target.positionX
    val remainingY = targetPosition.y - target.positionY
    val remainingZ = targetPosition.z - target.positionZ

    val remainingLengthSquared =
        remainingX * remainingX + remainingY * remainingY + remainingZ * remainingZ

    if (remainingLengthSquared < JumpPadLaunch.FINAL_SNAP_DISTANCE_SQUARED) {
        target.applyVelocity(remainingX, remainingY, remainingZ)
        target.resetFallDistance()
    }
}
