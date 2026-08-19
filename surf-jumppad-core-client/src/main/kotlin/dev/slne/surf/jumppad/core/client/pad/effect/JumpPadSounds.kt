package dev.slne.surf.jumppad.core.client.pad.effect

import dev.slne.surf.api.core.messages.adventure.playSound
import dev.slne.surf.jumppad.core.client.pad.JumpPadType
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.sound.Sound

/**
 * Plays the sound a jump pad makes when it is triggered.
 *
 * @param type the jump pad type defining the sound
 */
fun Audience.playJumpPadSound(type: JumpPadType) {
    playSound(true) {
        type(type.sound)
        source(Sound.Source.NEUTRAL)
        pitch(1.0f)
    }
}
