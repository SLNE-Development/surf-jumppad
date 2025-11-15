package dev.slne.surf.jumppad.listeners

import dev.slne.surf.jumppad.pad.JumpPadType
import dev.slne.surf.jumppad.pad.jumpPadService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.util.Vector

object PlayerMoveEvent : Listener {

    @EventHandler()
    fun onPlayerMove(event: PlayerMoveEvent) {
        if (!event.hasChangedPosition()) return

        val movedBlock = event.to
        val pad = jumpPadService.getPadAt(movedBlock)

        if (pad == null) return
        val player = event.player

        val velocity = when (pad.type) {
            JumpPadType.VERTICAL -> Vector(0.0, pad.strength, 0.0)
            JumpPadType.HORIZONTAL -> player.location.direction.normalize().multiply(pad.strength)
        }

        player.velocity = velocity
    }
}

