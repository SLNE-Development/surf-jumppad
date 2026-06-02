plugins {
    id("dev.slne.surf.api.gradle.paper-plugin")
}

version = findProperty("version") as String
group = "dev.slne.surf.jumppad"

surfPaperPluginApi {
    mainClass("dev.slne.surf.jumppad.BukkitMain")

    authors.add("Jo_field")
}
