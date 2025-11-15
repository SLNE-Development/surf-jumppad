package dev.slne.surf.jumppad

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import org.bukkit.plugin.java.JavaPlugin

class BukkitMain : SuspendingJavaPlugin() {
}

val plugin get() = JavaPlugin.getPlugin(BukkitMain::class.java)