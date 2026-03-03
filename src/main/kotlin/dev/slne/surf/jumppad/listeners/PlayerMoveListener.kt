package dev.slne.surf.jumppad.listeners

import com.github.benmanes.caffeine.cache.Caffeine
import dev.slne.surf.jumppad.pad.JumpPadType
import dev.slne.surf.jumppad.pad.service.JumpPadService
import dev.slne.surf.jumppad.pad.service.jumpPadBoostService
import dev.slne.surf.jumppad.particles.AnimationService
import dev.slne.surf.jumppad.sounds.soundService
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.util.Vector
import java.util.*
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

object PlayerMoveListener : Listener {

    private val jumpPadCooldowns = Caffeine.newBuilder()
        .expireAfterWrite(3.seconds.toJavaDuration())
        .build<UUID, UUID>()

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        if (!event.hasExplicitlyChangedBlock()) return
        val player = event.player

        if (event.player.gameMode == GameMode.SPECTATOR) return

        val pad = JumpPadService.getPadAt(event.to) ?: return

        val activeCooldownPadUuid = jumpPadCooldowns.getIfPresent(player.uniqueId)
        if (activeCooldownPadUuid == pad.uuid) return

        jumpPadCooldowns.put(player.uniqueId, pad.uuid)

        val startLoc = pad.originLocation.clone().add(0.5, 0.0, 0.5)

        val targetLoc: Location = when (pad.type) {
            JumpPadType.STATIC -> {
                pad.targetLocation ?: pad.originLocation.clone().add(0.0, 5.0, 0.0)
            }

            JumpPadType.VERTICAL -> {
                val height = pad.distance.toDouble()
                player.velocity = Vector(0.0, sqrt(2 * 0.08 * height), 0.0)
                AnimationService.playStartAnimation(player, pad.type)
                soundService.playSound(player, pad.type)
                return
            }

            else -> {
                val dir = pad.type.getDirection(player)
                pad.originLocation.clone().add(dir.multiply(pad.distance.toDouble()))
            }
        }.centerXZIfBlockAligned()

        val actualDistance = startLoc.distance(targetLoc)
        val peak = (actualDistance / 3.0).coerceAtLeast(3.0)

        jumpPadBoostService.startBoost(
            player = player,
            start = startLoc,
            target = targetLoc,
            peakHeight = peak,
            padType = pad.type
        )

        AnimationService.playStartAnimation(player, pad.type)
        soundService.playSound(player, pad.type)
    }

    private fun Location.centerXZIfBlockAligned(eps: Double = 1e-6): Location {
        val loc = this.clone()
        val bx = loc.blockX.toDouble()
        val bz = loc.blockZ.toDouble()

        if (abs(loc.x - bx) < eps) loc.x = bx + 0.5
        if (abs(loc.z - bz) < eps) loc.z = bz + 0.5

        return loc
    }
}