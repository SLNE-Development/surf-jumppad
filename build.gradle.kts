plugins {
    id("dev.slne.surf.surfapi.gradle.paper-plugin") version "1.21.11+"
}
surfPaperPluginApi {
    mainClass("dev.slne.surf.jumppad.PaperMain")
    generateLibraryLoader(false)
    foliaSupported(true)

    authors.add("Jo_field")
}

version = findProperty("version") as String
group = "dev.slne.surf.jumppad"