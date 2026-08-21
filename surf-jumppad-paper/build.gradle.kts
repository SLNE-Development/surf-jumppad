plugins {
    id("dev.slne.surf.api.gradle.paper-plugin")
}

surfPaperPluginApi {
    mainClass("dev.slne.surf.jumppad.BukkitMain")
    authors.add("Jo_field")
}

paper {
    name = "surf-jumppad" // Avoid data loss after module split
}

dependencies {
    api(projects.surfJumppadCoreClient)
}
