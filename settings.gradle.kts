pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.minecraftforge.net/") { name = "Forge" }
        maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
    }
}

plugins {
    // Preprocessor allowing a single codebase for every Minecraft version and mod loader (https://stonecutter.kikugie.dev/)
    id("dev.kikugie.stonecutter") version "0.9.8"

    // Picks the remapping (<= 1.21.11) or non-remapping (26.1+) loom variant depending on the Minecraft version
    id("dev.kikugie.loom-back-compat") version "0.4.2"

    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

stonecutter {
    create(rootProject) {
        /**
         * Creates one subproject per loader named `versions/{minecraft}-{loader}`,
         * built with the `build.{loader}.gradle.kts` script.
         */
        fun match(minecraft: String, vararg loaders: String) {
            for (loader in loaders) version("$minecraft-$loader", minecraft).buildscript("build.$loader.gradle.kts")
        }

        match("1.21.1", "fabric", "forge", "neoforge")
        match("1.21.11", "fabric", "forge", "neoforge")
        match("26.2", "fabric", "forge", "neoforge")
        match("26.3", "fabric", "forge", "neoforge")

        // Version the sources in `src/` are committed as
        vcsVersion = "26.3-fabric"
    }
}

rootProject.name = "mc2discord"

include(":mc2discord-core")
