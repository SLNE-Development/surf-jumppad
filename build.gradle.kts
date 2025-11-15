plugins {
    id("dev.slne.surf.surfapi.gradle.paper-plugin")
}

surfPaperPluginApi {
    mainClass("dev.slne.surf.jumppad.BukkitMain")
    authors.addAll("red", "Jo_field")
    foliaSupported(true)

    generateLibraryLoader(false)
}