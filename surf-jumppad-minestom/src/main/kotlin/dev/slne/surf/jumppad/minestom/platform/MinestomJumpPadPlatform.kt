package dev.slne.surf.jumppad.minestom.platform

import com.google.auto.service.AutoService
import dev.slne.surf.jumppad.core.client.platform.JumpPadPlatform
import dev.slne.surf.jumppad.minestom.pad.findInstance
import net.kyori.adventure.key.Key
import net.kyori.adventure.util.Services

@AutoService(JumpPadPlatform::class)
class MinestomJumpPadPlatform : JumpPadPlatform, Services.Fallback {
    override fun isKnownWorld(worldKey: Key): Boolean = findInstance(worldKey) != null
}
