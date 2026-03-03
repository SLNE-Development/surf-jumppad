package dev.slne.surf.jumppad.config

import dev.slne.surf.jumppad.plugin
import dev.slne.surf.surfapi.core.api.config.createSpongeYmlConfig
import dev.slne.surf.surfapi.core.api.config.manager.SpongeConfigManager
import dev.slne.surf.surfapi.core.api.config.surfConfigApi

object JumpPadConfigHolder {
    private val manager: SpongeConfigManager<JumpPadConfig>

    init {
        surfConfigApi.createSpongeYmlConfig<JumpPadConfig>(plugin.dataPath, "jumppads.yml")
        manager = surfConfigApi.getSpongeConfigManagerForConfig(JumpPadConfig::class.java)
    }

    val config: JumpPadConfig get() = manager.config

    fun save() {
        manager.save()
    }

    fun reload() {
        manager.reloadFromFile()
    }
}

val jumppadConfig get() = JumpPadConfigHolder.config