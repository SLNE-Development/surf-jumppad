package dev.slne.surf.jumppad.storage

import dev.slne.surf.api.core.util.logger
import dev.slne.surf.jumppad.core.client.storage.JumpPadStorage
import dev.slne.surf.jumppad.plugin
import kotlin.io.path.exists

object StorageService {
    private val log = logger()
    private val jumpPadsPath = plugin.dataPath.resolve(JumpPadStorage.FILE_NAME)
    private val storage = JumpPadStorage(jumpPadsPath)

    suspend fun loadPadsFromFile() {
        if (!jumpPadsPath.exists()) {
            migrateLegacyStorage()
            return
        }

        storage.loadPads()
    }

    @Suppress("DEPRECATION")
    private suspend fun migrateLegacyStorage() {
        storageServiceOld.init()
        if (!storageServiceOld.hasLegacyData()) return

        val migratedPads = storageServiceOld.loadPads()
        if (migratedPads == 0) {
            log.atWarning().log("Legacy pad files were found, but no pads could be migrated.")
            return
        }

        savePads()
        log.atInfo().log("Migrated $migratedPads legacy jump pads to ${jumpPadsPath.fileName}.")
    }

    suspend fun savePads() {
        storage.savePads()
    }
}
