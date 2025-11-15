package dev.slne.surf.jumppad.pad

import org.bukkit.Location
import java.util.UUID

class JumpPadManager {
    private val pads = mutableSetOf<JumpPad>()

    fun addPad(pad: JumpPad) {
        pads.add(pad)
    }

    fun getPadByUuid(uuid: UUID): JumpPad? {
        return pads.firstOrNull { it.uuid == uuid }
    }

    fun getPadAt(location: Location): JumpPad? {
        return pads.firstOrNull { pad ->
            val dx = location.blockX - pad.origin.blockX
            val dz = location.blockZ - pad.origin.blockZ
            dx in 0 until pad.size && dz in 0 until pad.size
        }
    }

    companion object {
        val INSTANCE = JumpPadManager()
    }
}

val jumpPadService get() = JumpPadManager.INSTANCE