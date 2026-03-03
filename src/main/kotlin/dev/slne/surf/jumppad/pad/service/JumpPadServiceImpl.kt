package dev.slne.surf.jumppad.pad.service

import com.google.auto.service.AutoService
import dev.slne.surf.jumppad.config.JumpPadConfigHolder
import dev.slne.surf.jumppad.config.jumppadConfig
import dev.slne.surf.jumppad.pad.JumpPad
import dev.slne.surf.surfapi.core.api.util.freeze
import dev.slne.surf.surfapi.core.api.util.mutableObjectListOf
import net.kyori.adventure.util.Services
import org.bukkit.Location
import java.util.*

@AutoService(JumpPadService::class)
class JumpPadServiceImpl : JumpPadService, Services.Fallback {
    private val _jumppads = mutableObjectListOf<JumpPad>()
    override val jumpPads get() = _jumppads.freeze()

    override val jumpPadCount get() = jumpPads.size

    override fun registerPads() {
        _jumppads.clear()
        _jumppads.addAll(jumppadConfig.teleporters)
    }

    override fun registerPad(jumpPad: JumpPad) {
        _jumppads.add(jumpPad)

        jumppadConfig.apply {
            teleporters.add(jumpPad)
        }
        JumpPadConfigHolder.save()
    }

    override fun unregisterPad(jumpPad: JumpPad) {
        _jumppads.remove(jumpPad)

        jumppadConfig.apply {
            teleporters.remove(jumpPad)
        }
        JumpPadConfigHolder.save()
    }

    override fun getPadAt(location: Location) = jumpPads.firstOrNull { teleporter ->
        val boundingBox = teleporter.boundingBox

        val originLoaded = teleporter.originLocation.isWorldLoaded
        val locationLoaded = location.isWorldLoaded
        if (!originLoaded || !locationLoaded) return@firstOrNull false

        val worldMatches = teleporter.originLocation.world.uid == location.world.uid
        val boundingBoxContains = boundingBox.contains(location.toVector())

        worldMatches && boundingBoxContains
    }

    override fun generateUnusedId(): UUID {
        var id: UUID
        do {
            id = UUID.randomUUID()
        } while (_jumppads.any { it.uuid == id })
        return id
    }

    override fun savePads() {
        JumpPadConfigHolder.save()
    }
}