package dev.slne.surf.jumppad.listeners

import dev.slne.surf.jumppad.pad.service.JumpPadBoostService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

object PlayerQuitListener : Listener {

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        JumpPadBoostService.stopBoost(event.player)
    }
}
