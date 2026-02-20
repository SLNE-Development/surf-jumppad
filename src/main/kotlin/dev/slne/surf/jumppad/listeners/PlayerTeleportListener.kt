package dev.slne.surf.jumppad.listeners

import dev.slne.surf.jumppad.pad.service.jumpPadBoostService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerTeleportEvent

object PlayerTeleportListener : Listener {

    @EventHandler
    fun onPlayerTeleport(event: PlayerTeleportEvent) {
        val player = event.player

        if (!jumpPadBoostService.isBoosting(player)) return

        jumpPadBoostService.stopBoost(event.player)
    }
}