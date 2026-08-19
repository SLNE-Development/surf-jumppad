package dev.slne.surf.jumppad.pad

import dev.slne.surf.api.paper.extensions.server
import dev.slne.surf.jumppad.core.client.pad.JumpPadPosition
import net.kyori.adventure.key.Key
import org.bukkit.Location
import org.bukkit.World

/**
 * Converts this location into a platform-neutral position.
 *
 * @return the position, or `null` if this location has no world
 */
fun Location.toJumpPadPosition(): JumpPadPosition? {
    val worldKey = world?.key() ?: return null
    return JumpPadPosition(worldKey, x, y, z, yaw, pitch)
}

/**
 * Converts this position into a location in [world].
 *
 * @param world the world the location belongs to
 * @return the location
 */
fun JumpPadPosition.toLocation(world: World): Location = Location(world, x, y, z, yaw, pitch)

/**
 * Converts this position into a location.
 *
 * @return the location, or `null` if the world of this position is not loaded
 */
fun JumpPadPosition.toLocation(): Location? = world()?.let { toLocation(it) }

/**
 * Returns the world this position belongs to.
 *
 * @return the world, or `null` if it is not loaded
 */
fun JumpPadPosition.world(): World? = findWorld(worldKey)

/**
 * Returns the world [worldKey] names.
 *
 * @param worldKey the key of the world
 * @return the world, or `null` if the key names no loaded world
 */
fun findWorld(worldKey: Key): World? = server.getWorld(worldKey)
