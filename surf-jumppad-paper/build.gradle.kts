plugins {
    id("dev.slne.surf.api.gradle.paper-plugin")
}

surfPaperPluginApi {
    mainClass("dev.slne.surf.jumppad.BukkitMain")

    authors.add("Jo_field")
}

dependencies {
    api(projects.surfJumppadCoreClient)
}
