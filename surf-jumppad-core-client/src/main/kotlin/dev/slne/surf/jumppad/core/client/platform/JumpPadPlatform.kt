package dev.slne.surf.jumppad.core.client.platform

import dev.slne.surf.api.core.util.requiredService
import net.kyori.adventure.key.Key

private val platform = requiredService<JumpPadPlatform>()

/**
 * The few things jump pads need from the server they run on.
 */
interface JumpPadPlatform {
    /**
     * Returns whether a world with the given key is loaded.
     *
     * @param worldKey the key of the world
     * @return `true` if the world exists on this server
     */
    fun isKnownWorld(worldKey: Key): Boolean

    companion object : JumpPadPlatform by platform
}
