package dev.slne.surf.jumppad.platform

import com.google.auto.service.AutoService
import dev.slne.surf.jumppad.core.client.platform.JumpPadPlatform
import dev.slne.surf.jumppad.pad.findWorld
import net.kyori.adventure.key.Key
import net.kyori.adventure.util.Services

@AutoService(JumpPadPlatform::class)
class PaperJumpPadPlatform : JumpPadPlatform, Services.Fallback {
    override fun isKnownWorld(worldKey: Key): Boolean = findWorld(worldKey) != null
}
