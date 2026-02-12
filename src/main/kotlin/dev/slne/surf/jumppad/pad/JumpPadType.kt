package dev.slne.surf.jumppad.pad

import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import net.kyori.adventure.text.Component
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.util.Vector

enum class JumpPadType(
    val displayComponent: Component,
    val particleEffect: Particle,
    val sound: Sound,
    val staticDirection: Vector? = null
) {
    HORIZONTAL_NORTH(buildText { text("Norden") }, Particle.SONIC_BOOM, Sound.ENTITY_WARDEN_SONIC_BOOM, Vector(0, 0, -1)),
    HORIZONTAL_EAST(buildText { text("Osten") }, Particle.SONIC_BOOM, Sound.ENTITY_WARDEN_SONIC_BOOM, Vector(1, 0, 0)),
    HORIZONTAL_SOUTH(buildText { text("Süden") }, Particle.SONIC_BOOM, Sound.ENTITY_WARDEN_SONIC_BOOM, Vector(0, 0, 1)),
    HORIZONTAL_WEST(buildText { text("Westen") }, Particle.SONIC_BOOM, Sound.ENTITY_WARDEN_SONIC_BOOM, Vector(-1, 0, 0)),
    VERTICAL(buildText { text("Vertikal") }, Particle.EXPLOSION, Sound.ENTITY_WIND_CHARGE_WIND_BURST, Vector(0, 1, 0)),

    PLAYER_DIRECTION(buildText { text("Spielerblick") }, Particle.HEART, Sound.ENTITY_WIND_CHARGE_WIND_BURST);

    fun getDirection(player: Player): Vector {
        return staticDirection?.clone() ?: when (this) {
            PLAYER_DIRECTION -> player.location.direction.clone().setY(0).normalize()
            else -> Vector(0, 0, 0)
        }
    }
}