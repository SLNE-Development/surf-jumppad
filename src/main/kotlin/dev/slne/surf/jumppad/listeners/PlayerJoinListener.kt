package dev.slne.surf.jumppad.listeners

import dev.slne.surf.jumppad.pad.jumpPadService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

object PlayerJoinListener : Listener {

    @EventHandler()
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        jumpPadService.visualizePadsForSpecific(player)
    }
}