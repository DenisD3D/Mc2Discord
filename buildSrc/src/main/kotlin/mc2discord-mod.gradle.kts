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

        // Transitive dependencies that are never loaded at runtime.
        // Every package left in the jar must be in the relocations below, and the other way around.
        dependencies {
            val unused = listOf(
                // Voice is not used: its encryption and DAVE stack is only reachable from 4 classes of discord4j-voice
                // (discord4j-voice itself is kept, discord4j-core references it)
                "com.google.crypto.tink:tink",
                "com.google.protobuf:protobuf-java",
                "moe.kyokobot.libdave:.*",
                "dev.arbjerg:lava-common",
                "com.discord4j:simple-fsm",
                // Only needed by the above and shipped with Minecraft: the core uses the Minecraft ones
                "com.google.code.gson:gson",
                "commons-io:commons-io",
                // Compile time only: annotations and annotation processors
                "com.google.code.findbugs:jsr305",
                "com.google.errorprone:error_prone_annotations",
                "org.checkerframework:checker-qual",
                "org.jspecify:jspecify",
                "org.immutables:encode",
                "com.austinv11.servicer:Servicer",
                // Legacy store, only found through META-INF/services which is excluded
                "com.discord4j:stores-jdk",
                // Native transports and HTTP/3 (QUIC): the natives can't be loaded once relocated,
                // reactor-netty detects their absence and uses NIO and HTTP/1.1.
                // netty-codec-classes-quic must stay, reactor-netty calls it to check if HTTP/3 is available
                "io.netty:netty-transport-native-epoll",
                "io.netty:netty-transport-classes-epoll",
                "io.netty:netty-resolver-dns-native-macos",
                "io.netty:netty-resolver-dns-classes-macos",
                "io.netty:netty-codec-native-quic",
                "io.netty:netty-codec-http3"
            )
            unused.forEach {
                exclude(dependency("$it:.*"))
            }
        }

        // Drop the classes that are not reachable from the mod, like the server side of reactor-netty.
        // Jackson and Caffeine load classes by name and must be kept whole.
        minimize {
            exclude(dependency("com.fasterxml.jackson.*:.*:.*"))
            exclude(dependency("com.github.ben-manes.caffeine:.*:.*"))
        }

        val relocateLocation = "fr.denisd3d.mc2discord.shadow"
        val relocations = listOf(
            // Discord4J
            "discord4j",
            "reactor",
            "org.reactivestreams",
            "io.netty",
            "com.fasterxml.jackson",
            "com.github.benmanes.caffeine",
            // Config
            "fr.denisd3d.config4j",
            "com.electronwill.nightconfig",
            // Messages
            "com.vdurmont.emoji",
            "org.json",
            // Account
            "org.apache.commons.collections4"
        )
        relocations.forEach {
            relocate(it, "$relocateLocation.$it")
        }
        exclude("META-INF/services/**") // Fix compatibility with geckolib
        exclude("META-INF/native/**") // Can't be loaded once relocated
        // Build metadata of the dependencies
        exclude("META-INF/maven/**")
        exclude("META-INF/native-image/**")
        exclude("META-INF/proguard/**")
        exclude("META-INF/jandex.idx")
        exclude("META-INF/versions/*/OSGI-INF/**")
        exclude("**/module-info.class")
    }

    assemble {
        dependsOn(shadowJar)
    }

    test {
        enabled = false
    }
}
