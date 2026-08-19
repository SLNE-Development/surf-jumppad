package dev.slne.surf.jumppad.minestom

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.slne.minestom.lobby.api.plugin.MinestomPluginEntrypoint
import dev.slne.minestom.lobby.api.plugin.annotation.DataDirectory
import dev.slne.surf.jumppad.minestom.storage.StorageService
import java.nio.file.Path

@Singleton
class SurfJumpPadMinestomEntrypoint @Inject constructor(
    @DataDirectory path: Path
) : MinestomPluginEntrypoint {

    init {
        dataPath = path
    }

    override suspend fun start() {
        StorageService.loadPads()
    }

    override suspend fun stop() {
        StorageService.savePads()
    }

    companion object {
        lateinit var dataPath: Path
            private set
    }
}
