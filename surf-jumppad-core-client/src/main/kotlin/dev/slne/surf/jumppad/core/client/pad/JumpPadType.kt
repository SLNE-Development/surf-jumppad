package dev.slne.surf.jumppad.core.client.pad

import dev.slne.surf.api.core.messages.adventure.buildText
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.spongepowered.math.vector.Vector3d

private val EXPLOSION_PARTICLE = Key.key("explosion")
private val CLOUD_PARTICLE = Key.key("cloud")
private val WIND_BURST_SOUND = Key.key("entity.wind_charge.wind_burst")

/**
 * The kinds of jump pads and the behavior each of them launches players with.
 *
 * Effects are described by their key so that every platform can resolve them against its own
 * particle and sound types.
 *
 * @property displayComponent the name shown to players
 * @property particleEffect the particle spawned when a jump pad is triggered
 * @property particleBoostEffect the particle spawned along the flight path
 * @property sound the sound played when a jump pad is triggered
 * @property staticDirection the fixed launch direction, or `null` if the direction is derived
 */
enum class JumpPadType(
    val displayComponent: Component,
    val particleEffect: Key,
    val particleBoostEffect: Key,
    val sound: Key,
    val staticDirection: Vector3d? = null
) {
    HORIZONTAL_NORTH(
        buildText { text("Norden", TextColor.color(52, 152, 219)) },
        EXPLOSION_PARTICLE,
        CLOUD_PARTICLE,
        WIND_BURST_SOUND,
        Vector3d(0.0, 0.0, -1.0)
    ),

    HORIZONTAL_EAST(
        buildText { text("Osten", TextColor.color(52, 152, 219)) },
        EXPLOSION_PARTICLE,
        CLOUD_PARTICLE,
        WIND_BURST_SOUND,
        Vector3d(1.0, 0.0, 0.0)
    ),

    HORIZONTAL_SOUTH(
        buildText { text("Süden", TextColor.color(52, 152, 219)) },
        EXPLOSION_PARTICLE,
        CLOUD_PARTICLE,
        WIND_BURST_SOUND,
        Vector3d(0.0, 0.0, 1.0)
    ),

    HORIZONTAL_WEST(
        buildText { text("Westen", TextColor.color(52, 152, 219)) },
        EXPLOSION_PARTICLE,
        CLOUD_PARTICLE,
        WIND_BURST_SOUND,
        Vector3d(-1.0, 0.0, 0.0)
    ),

    VERTICAL(
        buildText { text("Vertikal", TextColor.color(46, 204, 113)) },
        EXPLOSION_PARTICLE,
        CLOUD_PARTICLE,
        WIND_BURST_SOUND,
        Vector3d(0.0, 1.0, 0.0)
    ),

    STATIC(
        buildText { text("Zielpunkt", TextColor.color(3, 252, 198)) },
        EXPLOSION_PARTICLE,
        CLOUD_PARTICLE,
        WIND_BURST_SOUND
    ),

    PLAYER_DIRECTION(
        buildText { text("Spielerblick", TextColor.color(230, 126, 34)) },
        EXPLOSION_PARTICLE,
        CLOUD_PARTICLE,
        WIND_BURST_SOUND
    ) {
        override fun getDirection(playerDirection: Vector3d): Vector3d {
            return Vector3d(playerDirection.x(), 0.0, playerDirection.z()).normalize()
        }
    };

    /**
     * Returns the direction a player is launched into.
     *
     * @param playerDirection the direction the player is currently looking at
     * @return the launch direction of this jump pad type
     */
    open fun getDirection(playerDirection: Vector3d): Vector3d {
        return staticDirection ?: Vector3d.ZERO
    }
}
