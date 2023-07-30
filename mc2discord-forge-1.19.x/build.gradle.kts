import java.util.Properties

plugins {
    id("java")
    id("idea")
    id("net.minecraftforge.gradle") version ("5.1.+")
    id("org.parchmentmc.librarian.forgegradle") version ("1.+")
    id("com.github.johnrengelman.shadow") version ("7.1.2")
    id("org.spongepowered.mixin") version ("0.7.+")
}

fun readProperties(propertiesFile: File) = Properties().apply {
    propertiesFile.inputStream().use { fis ->
        load(fis)
    }
}

val sharedProperties = readProperties(file("../shared.properties"))

val modId: String by sharedProperties
val modName: String by sharedProperties
val modGroup: String by sharedProperties
val modVersion: String = System.getenv("INPUT_VERSION") ?: "0.0.0-dev"
val modAuthors: String by sharedProperties
val modDescription: String by sharedProperties
val modIssueTrackerUrl: String by sharedProperties
val modUpdateJsonUrl: String by sharedProperties
val modDisplayUrl: String by sharedProperties
val discord4jVersion: String by sharedProperties

val javaVersion: String by extra
val forgeVersion: String by extra
val forgeVersionRange: String by extra
val minecraftVersion: String by extra
val minecraftVersionRange: String by extra
val minecraftDisplayVersion: String by extra
val parchmentMappingVersion: String by extra

version = modVersion
group = modGroup

base {
    archivesName.set("${modId}-forge-${minecraftDisplayVersion}")
}

mixin {
    add(sourceSets.main.get(), "${modId}.refmap.json")
    config("${modId}.mixins.json")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }
}


val shadowMinecraftLibrary: Configuration by configurations.creating
configurations.minecraftLibrary {
    extendsFrom(shadowMinecraftLibrary)
}

repositories {
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://jitpack.io")
    mavenCentral()
}

dependencies {
    minecraft(group = "net.minecraftforge", name = "forge", version = "${minecraftVersion}-${forgeVersion}")

    shadowMinecraftLibrary(project(":mc2discord-core")) {
        exclude(group = "com.electronwill.night-config")
    }

    annotationProcessor("org.spongepowered:mixin:0.8.5:processor") // Mixins

    @Suppress("VulnerableDependency")
    minecraftLibrary(group = "io.netty", name = "netty-all", version = "4.1.68.Final") // Fix compatibility with Discord4J in dev

    compileOnly(group = "com.discord4j", name = "discord4j-core", version = discord4jVersion)
}

minecraft {
    mappings("parchment", parchmentMappingVersion)

    accessTransformer(file("src/main/resources/META-INF/accesstransformer.cfg"))

    runs {
        create("server") {
            taskName("Server")
            property("forge.logging.console.level", "info")
            arg("nogui")
            singleInstance(true)
            workingDirectory(file("run/server"))
            mods {
                create(modId) {
                    source(sourceSets.main.get())
                }
            }
        }
    }
}

tasks {
    jar {
        archiveClassifier.set("slim")
        manifest {
            attributes(mapOf("Specification-Title" to modName,
                    "Specification-Vendor" to modAuthors,
                    "Specification-Version" to "1",
                    "Implementation-Title" to project.name,
                    "Implementation-Version" to modVersion,
                    "Implementation-Vendor" to modAuthors))
        }

    }

    shadowJar {
        archiveClassifier.set("")
        configurations = listOf(shadowMinecraftLibrary)
        val relocateLocation = "$modGroup.shadow"
        val relocations = listOf("io.netty", "reactor", "discord4j", "org.reactivestreams", "org.checkerframework", "com.iwebpp.crypto", "com.google.errorprone", "com.github.benmanes.caffeine", "com.fasterxml.jackson", "com.discord4j.fsm", "com.austinv11.servicer", "com.vdurmont.emoji", "ml.denisd3d.config4j", "org.apache.commons.collections4", "org.immutables.encode", "org.json")
        relocations.forEach {
            relocate(it, "$relocateLocation.$it")
        }
        exclude("META-INF/services/**") // Fix compatibility with geckolib
    }

    assemble {
        dependsOn(shadowJar)
    }

    reobf {
        create("shadowJar")
    }

    val resourceTargets = listOf("META-INF/mods.toml", "pack.mcmeta", "${modId}.mixins.json")
    val intoTargets = listOf("$rootDir/out/production/resources/", "$rootDir/out/production/${project.name}.main/", "$rootDir/bin/main/")
    val replaceProperties = mapOf("modId" to modId,
            "modName" to modName,
            "modGroup" to modGroup,
            "modVersion" to modVersion,
            "modAuthors" to modAuthors,
            "modDescription" to modDescription,
            "modIssueTrackerUrl" to modIssueTrackerUrl,
            "modUpdateJsonUrl" to modUpdateJsonUrl,
            "modDisplayUrl" to modDisplayUrl,
            "discord4jVersion" to discord4jVersion,

            "javaVersion" to javaVersion,
            "forgeVersion" to forgeVersion,
            "forgeVersionRange" to forgeVersionRange,
            "minecraftVersion" to minecraftVersion,
            "minecraftVersionRange" to minecraftVersionRange,
            "minecraftDisplayVersion" to minecraftDisplayVersion,
            "parchmentMappingVersion" to parchmentMappingVersion)

    processResources {
        inputs.properties(replaceProperties)

        filesMatching(resourceTargets) {
            expand(replaceProperties)
        }
        intoTargets.forEach { target ->
            if (file(target).exists()) {
                copy {
                    from(sourceSets.main.get().resources) {
                        include(resourceTargets)
                        expand(replaceProperties)
                    }
                    into(target)
                }
            }
        }
    }
}