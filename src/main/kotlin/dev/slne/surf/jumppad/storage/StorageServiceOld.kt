package dev.slne.surf.jumppad.storage

import com.google.auto.service.AutoService
import dev.slne.surf.api.core.util.logger
import dev.slne.surf.jumppad.pad.JumpPad
import dev.slne.surf.jumppad.pad.JumpPadType
import dev.slne.surf.jumppad.pad.service.JumpPadService
import dev.slne.surf.jumppad.plugin
import org.bukkit.configuration.file.YamlConfiguration
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

@Deprecated("This is the old storage service, use the new one instead")
@AutoService(StorageServiceOld::class)
class StorageServiceOld {
    private val log = logger()
    private val jumpPadFolder: Path get() = plugin.dataPath.resolve("pads")

    fun init() {
        if (!Files.exists(jumpPadFolder)) {
            Files.createDirectories(jumpPadFolder)
        }
    }

    fun hasLegacyData(): Boolean = getPadFiles().isNotEmpty()

    fun loadPads(): Int {
        val files = getPadFiles()
        var loadedCount = 0

        files.forEach { path ->
            val config = YamlConfiguration.loadConfiguration(path.toFile())

            runCatching {
                val uuidString = config.getString("pad.data.uuid") ?: error("UUID missing in ${path.fileName}")
                val uuid = UUID.fromString(uuidString)
                val origin = config.getLocation("pad.data.origin") ?: error("Origin missing in ${path.fileName}")

                val typeString = config.getString("pad.data.type") ?: error("Type missing in ${path.fileName}")
                val type = JumpPadType.valueOf(typeString.uppercase())

                val distance = config.getInt("pad.data.distance")
                val targetLocation = config.getLocation("pad.data.targetLocation")

                val width = config.getInt("pad.data.width")
                val length = config.getInt("pad.data.length")

                val pad = JumpPad(uuid, origin, type, distance, targetLocation, width, length)
                if (JumpPadService.registerPad(pad)) {
                    loadedCount++
                } else {
                    log.atWarning().log("Pad with UUID ${pad.uuid} already exists. Skipping this pad.")
                }
            }.onFailure {
                log.atWarning()
                    .withCause(it)
                    .log("Failed to load JumpPad from file ${path.fileName}: ${it.message}")
            }
        }
        log.atInfo().log("Successfully loaded $loadedCount JumpPads from ${files.size} files!")
        return loadedCount
    }

    fun savePads() {
        getPadFiles().forEach(Files::delete)

        val pads = JumpPadService.getPads()

        pads.forEach { pad ->
            val file = jumpPadFolder.resolve("${pad.uuid}.yml").toFile()
            val config = YamlConfiguration()

            config["pad.data.uuid"] = pad.uuid.toString()
            config["pad.data.origin"] = pad.origin
            config["pad.data.type"] = pad.type.name
            config["pad.data.distance"] = pad.distance
            pad.targetLocation?.let { config["pad.data.targetLocation"] = it }
            config["pad.data.width"] = pad.width
            config["pad.data.length"] = pad.length

            config.save(file)
        }

        log.atInfo().log("Successfully saved ${pads.size} JumpPads to files!")
    }

    private fun getPadFiles(): List<Path> {
        init()
        return Files.list(jumpPadFolder).use { stream ->
            stream
                .filter { it.fileName.toString().endsWith(".yml") }
                .toList()
        }
    }

    companion object {
        val instance = StorageServiceOld()
    }
}

@Deprecated("This is the old storage service, use the new one instead")
val storageServiceOld get() = StorageServiceOld.instance
