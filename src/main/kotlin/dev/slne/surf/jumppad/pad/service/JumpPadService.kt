package dev.slne.surf.jumppad.pad.service

import dev.slne.surf.jumppad.pad.JumpPad
import dev.slne.surf.surfapi.core.api.util.requiredService
import it.unimi.dsi.fastutil.objects.ObjectList
import org.bukkit.Location
import org.jetbrains.annotations.Unmodifiable
import java.util.*

private val jumppadService = requiredService<JumpPadService>()

interface JumpPadService {
    val jumpPadCount: Int
    val jumpPads: @Unmodifiable ObjectList<JumpPad>

    fun registerPads()
    fun registerPad(jumpPad: JumpPad)
    fun unregisterPad(jumpPad: JumpPad)

    fun getPadAt(location: Location): JumpPad?
    fun savePads()

    fun generateUnusedId(): UUID

    companion object : JumpPadService by jumppadService
}