import dev.slne.surf.api.gradle.util.slneReleases

plugins {
    id("dev.slne.surf.api.gradle.minestom")
}

surfMinestomApi {
    withCoreMinestom()
}

dependencies {
    api(projects.surfJumppadCoreClient)
}

publishing {
    repositories {
        slneReleases()
    }
}
