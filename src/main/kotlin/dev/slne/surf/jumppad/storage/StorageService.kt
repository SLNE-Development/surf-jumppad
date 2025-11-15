package dev.slne.surf.jumppad.storage

import com.google.auto.service.AutoService
import dev.slne.surf.jumppad.pad.JumpPad
import dev.slne.surf.jumppad.pad.JumpPadType
import dev.slne.surf.jumppad.pad.jumpPadService
import dev.slne.surf.jumppad.plugin
import dev.slne.surf.surfapi.core.api.util.logger
import org.bukkit.configuration.file.YamlConfiguration
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

@AutoService(StorageService::class)
class StorageService {
    private val jumpPadFolder: Path get() = plugin.dataPath.resolve("pads")

    fun init() {
        if (!Files.exists(jumpPadFolder)) {
            Files.createDirectories(jumpPadFolder)
        }
    }

    fun loadPads() {
        val files = Files.list(jumpPadFolder).filter { it.toString().endsWith(".yml") }.toList()
        var loadedCount = 0

        files.forEach { path ->
            val config = YamlConfiguration.loadConfiguration(path.toFile())

            runCatching {
                val uuid = UUID.fromString(config.getString("pad.data.uuid"))
                val origin = config.getLocation("pad.data.origin") ?: error("Origin missing in ${path.fileName}")
                val strength = config.getDouble("pad.data.strength")
                val width = config.getInt("pad.data.width")
                val length = config.getInt("pad.data.length")

                val typeString = config.getString("pad.data.type") ?: error("Type missing in ${path.fileName}")
                val type = JumpPadType.valueOf(typeString.uppercase()) // ???

                val pad = JumpPad(uuid, origin, strength, width, length, type)
                jumpPadService.registerPad(pad)
                loadedCount++
            }.onFailure {
                logger().atWarning().log("Failed to load JumpPad from file ${path.fileName}: ${it.message}")
            }
        }
        logger().atInfo().log("Successfully loaded $loadedCount JumpPads from ${files.size} files!")
    }

    fun savePads() {
        Files.list(jumpPadFolder)
            .filter { it.toString().endsWith(".yml") }
            .forEach(Files::delete)

        val pads = jumpPadService.getPads()

        pads.forEach { pad ->
            val file = jumpPadFolder.resolve("${pad.uuid}.yml").toFile()
            val config = YamlConfiguration()

            config["pad.data.uuid"] = pad.uuid.toString()
            config["pad.data.origin"] = pad.origin
            config["pad.data.strength"] = pad.strength
            config["pad.data.width"] = pad.width
            config["pad.data.length"] = pad.length
            config["pad.data.type"] = pad.type.name

            config.save(file)
        }

        logger().atInfo().log("Successfully saved ${pads.size} NPCs to files!")
    }

    companion object {
        val instance = StorageService()
    }
}

val storageService get() = StorageService.instance