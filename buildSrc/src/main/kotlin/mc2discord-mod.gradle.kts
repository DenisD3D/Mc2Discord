import java.text.SimpleDateFormat
import java.util.Date

// Build logic shared by every Minecraft version and mod loader.
// Projects are named `{minecraft}-{loader}`, see settings.gradle.kts.

plugins {
    id("java")
    id("idea")
    id("com.gradleup.shadow")
}

val minecraftVersion = name.substringBeforeLast('-')
val loader = name.substringAfterLast('-')

fun prop(key: String) = property(key) as String
fun optionalProp(key: String) = findProperty(key) as String? ?: ""

val modVersion: String = System.getenv("INPUT_VERSION") ?: "0.0.0-dev"

version = modVersion
base.archivesName.set("${prop("mod.id")}-$loader-${prop("mod.mc_display")}")

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(prop("mod.java")))
}

repositories {
    maven("https://jitpack.io")
    mavenCentral()
}

// Dependencies bundled and relocated in the mod jar
val shade: Configuration by configurations.creating {
    isCanBeConsumed = false
    // Only bundle what is needed at runtime, whatever the mod loader plugin does with the configurations
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
    }
}
configurations.implementation.get().extendsFrom(shade)

dependencies {
    shade(project(":mc2discord-core"))
}

tasks {
    jar {
        manifest {
            attributes(
                "Specification-Title" to prop("mod.name"),
                "Specification-Vendor" to prop("mod.authors"),
                "Specification-Version" to modVersion,
                "Implementation-Title" to prop("mod.name"),
                "Implementation-Vendor" to prop("mod.authors"),
                "Implementation-Version" to modVersion,
                "Implementation-Timestamp" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date()),
                "Timestamp" to System.currentTimeMillis(),
                "Built-On-Java" to "${System.getProperty("java.vm.version")} (${System.getProperty("java.vm.vendor")})",
                "Build-On-Minecraft" to minecraftVersion
            )
        }
    }

    processResources {
        val properties = mapOf(
            "modId" to prop("mod.id"),
            "modName" to prop("mod.name"),
            "modVersion" to modVersion,
            "modAuthors" to prop("mod.authors"),
            "modDescription" to prop("mod.description"),
            "modIssueTrackerUrl" to prop("mod.issue_tracker_url"),
            "modUpdateJsonUrl" to prop("mod.update_json_url"),
            "modDisplayUrl" to prop("mod.display_url"),
            "javaVersion" to prop("mod.java"),
            "minecraftVersionRange" to prop("mod.mc_range"),
            "loaderVersionRange" to optionalProp("mod.loader_range"),
            "fabricLoaderVersion" to optionalProp("deps.fabric_loader")
        )
        inputs.properties(properties)

        filesMatching(listOf("pack.mcmeta", "fabric.mod.json", "META-INF/mods.toml", "META-INF/neoforge.mods.toml")) {
            expand(properties)
        }

        // The resources of every loader live in the same directory, only keep the ones of the current loader
        if (loader != "fabric") exclude("fabric.mod.json", "*.accesswidener", "*.fabric.mixins.json")
        if (loader != "forge") exclude("META-INF/mods.toml")
        if (loader != "neoforge") exclude("META-INF/neoforge.mods.toml")
        if (loader == "fabric") exclude("META-INF/accesstransformer.cfg")
    }

    shadowJar {
        configurations = listOf(shade)
        val relocateLocation = "fr.denisd3d.mc2discord.shadow"
        val relocations = listOf(
            "io.netty",
            "reactor",
            "discord4j",
            "org.reactivestreams",
            "org.checkerframework",
            "com.iwebpp.crypto",
            "com.google.errorprone",
            "com.github.benmanes.caffeine",
            "com.fasterxml.jackson",
            "com.discord4j.fsm",
            "com.austinv11.servicer",
            "com.vdurmont.emoji",
            "fr.denisd3d.config4j",
            "org.apache.commons.collections4",
            "org.immutables.encode",
            "org.json",
            "com.electronwill.nightconfig",
            "com.google.gson",
            "google.protobuf",
            "javax.annotation",
            "com.google.crypto.tink",
            "com.google.protobuf",
            "org.apache.commons.io",
            "org.jspecify.annotations",
            "moe.kyokobot.libdave",
            "com.sedmelluq.lava"
        )
        relocations.forEach {
            relocate(it, "$relocateLocation.$it")
        }
        exclude("META-INF/services/**") // Fix compatibility with geckolib
        exclude("natives/**")
        exclude("META-INF/native/**")
    }

    assemble {
        dependsOn(shadowJar)
    }

    test {
        enabled = false
    }
}
