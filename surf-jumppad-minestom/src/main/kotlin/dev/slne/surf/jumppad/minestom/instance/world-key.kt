package dev.slne.surf.jumppad.minestom.instance

import net.kyori.adventure.key.Key
import net.minestom.server.instance.Instance
import net.minestom.server.tag.Tag

/**
 * The tag the lobby server stores the key of the world an instance was loaded from under.
 */
private val WORLD_KEY = Tag.String("surf_lobby_world_key")

/**
 * The key of the stored world this instance was loaded from, or `null` if it has none.
 */
val Instance.worldKey: Key?
    get() = getTag(WORLD_KEY)?.let { Key.key(it) }
