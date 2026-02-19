package dev.slne.surf.jumppad.pad

import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.util.Vector

enum class JumpPadType(
    val displayComponent: Component,
    val particleEffect: Particle,
    val particleBoostEffect: Particle,
    val sound: Sound,
    val staticDirection: Vector? = null
) {
    HORIZONTAL_NORTH(
        buildText { text("Norden", TextColor.color(52, 152, 219)) },
        Particle.EXPLOSION,
        Particle.CLOUD,
        Sound.ENTITY_WIND_CHARGE_WIND_BURST,
        Vector(0, 0, -1)
    ),

    HORIZONTAL_EAST(
        buildText { text("Osten", TextColor.color(52, 152, 219)) },
        Particle.EXPLOSION,
        Particle.CLOUD,
        Sound.ENTITY_WIND_CHARGE_WIND_BURST,
        Vector(1, 0, 0)
    ),

    HORIZONTAL_SOUTH(
        buildText { text("Süden", TextColor.color(52, 152, 219)) },
        Particle.EXPLOSION,
        Particle.CLOUD,
        Sound.ENTITY_WIND_CHARGE_WIND_BURST,
        Vector(0, 0, 1)
    ),

    HORIZONTAL_WEST(
        buildText { text("Westen", TextColor.color(52, 152, 219)) },
        Particle.EXPLOSION,
        Particle.CLOUD,
        Sound.ENTITY_WIND_CHARGE_WIND_BURST,
        Vector(-1, 0, 0)
    ),

    VERTICAL(
        buildText { text("Vertikal", TextColor.color(46, 204, 113)) },
        Particle.EXPLOSION,
        Particle.CLOUD,
        Sound.ENTITY_WIND_CHARGE_WIND_BURST,
        Vector(0, 1, 0)
    ),

    STATIC(
        buildText { text("Zielpunkt", TextColor.color(3, 252, 198)) },
        Particle.EXPLOSION,
        Particle.CLOUD,
        Sound.ENTITY_WIND_CHARGE_WIND_BURST
    ),

    PLAYER_DIRECTION(
        buildText { text("Spielerblick", TextColor.color(230, 126, 34)) },
        Particle.EXPLOSION,
        Particle.CLOUD,
        Sound.ENTITY_WIND_CHARGE_WIND_BURST
    );

    fun getDirection(player: Player): Vector {
        return staticDirection?.clone() ?: when (this) {
            PLAYER_DIRECTION -> player.location.direction.clone().setY(0).normalize()
            else -> Vector(0, 0, 0)
        }
    }
}