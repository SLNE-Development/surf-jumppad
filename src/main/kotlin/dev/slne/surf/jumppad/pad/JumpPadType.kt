package dev.slne.surf.jumppad.pad

import dev.slne.surf.api.core.messages.adventure.buildText
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.spongepowered.math.vector.Vector3d

enum class JumpPadType(
    val displayComponent: Component,
    val particleEffect: Particle,
    val particleBoostEffect: Particle,
    val sound: Sound,
    val staticDirection: Vector3d? = null
) {
    HORIZONTAL_NORTH(
        buildText { text("Norden", TextColor.color(52, 152, 219)) },
        Particle.EXPLOSION,
        Particle.CLOUD,
        Sound.ENTITY_WIND_CHARGE_WIND_BURST,
        Vector3d(0.0, 0.0, -1.0)
    ),

    HORIZONTAL_EAST(
        buildText { text("Osten", TextColor.color(52, 152, 219)) },
        Particle.EXPLOSION,
        Particle.CLOUD,
        Sound.ENTITY_WIND_CHARGE_WIND_BURST,
        Vector3d(1.0, 0.0, 0.0)
    ),

    HORIZONTAL_SOUTH(
        buildText { text("Süden", TextColor.color(52, 152, 219)) },
        Particle.EXPLOSION,
        Particle.CLOUD,
        Sound.ENTITY_WIND_CHARGE_WIND_BURST,
        Vector3d(0.0, 0.0, 1.0)
    ),

    HORIZONTAL_WEST(
        buildText { text("Westen", TextColor.color(52, 152, 219)) },
        Particle.EXPLOSION,
        Particle.CLOUD,
        Sound.ENTITY_WIND_CHARGE_WIND_BURST,
        Vector3d(-1.0, 0.0, 0.0)
    ),

    VERTICAL(
        buildText { text("Vertikal", TextColor.color(46, 204, 113)) },
        Particle.EXPLOSION,
        Particle.CLOUD,
        Sound.ENTITY_WIND_CHARGE_WIND_BURST,
        Vector3d(0.0, 1.0, 0.0)
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
    ) {
        override fun getDirection(player: Player): Vector3d {
            val playerDirection = player.location.direction
            return Vector3d(playerDirection.x, 0.0, playerDirection.z).normalize()
        }
    };

    open fun getDirection(player: Player): Vector3d {
        return staticDirection ?: Vector3d.ZERO
    }
}