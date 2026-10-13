import dev.slne.surf.api.gradle.util.slneReleases

plugins {
    id("dev.slne.surf.api.gradle.minestom")
}

repositories {
    mavenLocal()
}

surfMinestomApi {
    withCoreMinestom()
}

minestomPluginFile {
    main = "dev.slne.surf.jumppad.minestom.SurfJumpPadMinestomPlugin"
    authors = listOf("Jo_field")
}

dependencies {
    api(projects.surfJumppadCoreClient)
}

publishing {
    repositories {
        slneReleases()
    }
}
