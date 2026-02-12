package dev.slne.surf.jumppad.sounds

import dev.slne.surf.jumppad.pad.JumpPadType
import dev.slne.surf.surfapi.core.api.messages.adventure.playSound
import org.bukkit.entity.Player
import net.kyori.adventure.sound.Sound as AdventureSound
import org.bukkit.Sound as BukkitSound

val soundService = SoundManager

object SoundManager {
    fun playSound(player: Player, type: JumpPadType) {
        player.playSound(true) {
            type(type.sound)
            source(AdventureSound.Source.NEUTRAL)
            pitch(1.0f)
        }
    }
}