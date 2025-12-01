package dev.slne.surf.jumppad.sounds

import dev.slne.surf.jumppad.pad.JumpPadType
import dev.slne.surf.surfapi.core.api.messages.adventure.playSound
import org.bukkit.entity.Player
import net.kyori.adventure.sound.Sound as AdventureSound
import org.bukkit.Sound as BukkitSound

class SoundManager {
    fun playSound(player: Player, type: JumpPadType) {
        when (type) {
            JumpPadType.HORIZONTAL_EAST -> playHorizontal(player)
            JumpPadType.HORIZONTAL_WEST -> playHorizontal(player)
            JumpPadType.HORIZONTAL_NORTH -> playHorizontal(player)
            JumpPadType.HORIZONTAL_SOUTH -> playHorizontal(player)
            JumpPadType.VERTICAL -> playVertical(player)
        }
    }

    fun playHorizontal(player: Player) {
        player.playSound(true) {
            type(BukkitSound.ENTITY_WARDEN_SONIC_BOOM)
            source(AdventureSound.Source.NEUTRAL)
            pitch(1.0f)
        }
    }

    fun playVertical(player: Player) {
        player.playSound(true) {
            type(BukkitSound.ENTITY_WIND_CHARGE_WIND_BURST)
            source(AdventureSound.Source.NEUTRAL)
            pitch(1.0f)
        }
    }

    companion object {
        val INSTANCE = SoundManager()
    }
}

val soundService get() = SoundManager.INSTANCE