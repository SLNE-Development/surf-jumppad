package dev.slne.surf.jumppad.minestom.storage

import dev.slne.surf.jumppad.core.client.storage.JumpPadStorage
import dev.slne.surf.jumppad.minestom.SurfJumpPadMinestomEntrypoint

object StorageService {
    private val storage by lazy {
        JumpPadStorage.inDirectory(SurfJumpPadMinestomEntrypoint.dataPath)
    }

    suspend fun loadPads() {
        storage.loadPads()
    }

    suspend fun savePads() {
        storage.savePads()
    }
}
