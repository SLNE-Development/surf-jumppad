package dev.slne.surf.jumppad.sounds

import dev.slne.surf.api.core.messages.adventure.playSound
import dev.slne.surf.jumppad.pad.JumpPadType
import org.bukkit.entity.Player
import net.kyori.adventure.sound.Sound as AdventureSound

object SoundService {
    fun playSound(player: Player, type: JumpPadType) {
        player.playSound(true) {
            type(type.sound)
            source(AdventureSound.Source.NEUTRAL)
            pitch(1.0f)
        }
    }
}