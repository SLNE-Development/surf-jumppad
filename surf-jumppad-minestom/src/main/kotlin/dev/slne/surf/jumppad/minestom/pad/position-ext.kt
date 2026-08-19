package dev.slne.surf.jumppad.minestom.pad

import dev.slne.minestom.lobby.api.extension.InstanceManager
import dev.slne.minestom.lobby.api.instance.worldKey
import dev.slne.surf.jumppad.core.client.pad.JumpPadPosition
import net.kyori.adventure.key.Key
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Entity
import net.minestom.server.instance.Instance

/**
 * Converts this position into a platform-neutral position in [instance].
 *
 * @param instance the instance the position belongs to
 * @return the position, or `null` if the instance holds a world without an identity
 */
fun Point.toJumpPadPosition(instance: Instance): JumpPadPosition? {
    val worldKey = instance.worldKey ?: return null
    val yaw = (this as? Pos)?.yaw() ?: 0f
    val pitch = (this as? Pos)?.pitch() ?: 0f

    return JumpPadPosition(worldKey, x(), y(), z(), yaw, pitch)
}

/**
 * Converts the position this entity stands at into a platform-neutral position.
 *
 * @return the position, or `null` if the entity is in no instance, or in one whose world has no
 * identity
 */
fun Entity.toJumpPadPosition(): JumpPadPosition? =
    position.toJumpPadPosition(instance ?: return null)

/**
 * Converts this position into a Minestom position.
 *
 * @return the position
 */
fun JumpPadPosition.toPos(): Pos = Pos(x, y, z, yaw, pitch)

/**
 * Returns the instance holding the world this position belongs to.
 *
 * @return the instance, or `null` if no loaded instance holds that world
 */
fun JumpPadPosition.instance(): Instance? = findInstance(worldKey)

/**
 * Returns the instance holding the world [worldKey] names.
 *
 * @param worldKey the key of the world
 * @return the instance, or `null` if no loaded instance holds that world
 */
fun findInstance(worldKey: Key): Instance? =
    InstanceManager.instances.firstOrNull { it.worldKey == worldKey }
