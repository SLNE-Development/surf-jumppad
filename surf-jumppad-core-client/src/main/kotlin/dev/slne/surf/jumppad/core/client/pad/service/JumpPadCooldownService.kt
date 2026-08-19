package dev.slne.surf.jumppad.core.client.pad.service

import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.expireAfterWrite
import java.util.*
import kotlin.time.Duration.Companion.seconds

/**
 * Keeps players from triggering jump pads again right after they used one.
 *
 * A player who is allowed through starts a new cooldown; a player who is still on cooldown is
 * turned away without their cooldown being extended.
 */
object JumpPadCooldownService {

    private val cooldowns = Caffeine.newBuilder()
        .expireAfterWrite(3.seconds)
        .build<UUID, Unit>()

    /**
     * Starts a new cooldown for the given player, unless one is still running.
     *
     * @param playerId the unique id of the player
     * @return `true` if the player can use a jump pad, otherwise `false`
     */
    fun tryUse(playerId: UUID): Boolean {
        return cooldowns.asMap().putIfAbsent(playerId, Unit) == null
    }
}
