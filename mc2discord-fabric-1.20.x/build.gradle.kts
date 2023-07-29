import java.util.Properties

plugins {
    id("java")
    id("idea")
    id("fabric-loom") version ("1.3-SNAPSHOT")
    id("com.github.johnrengelman.shadow") version ("7.1.2")
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
val modMinecraftVersion: String by sharedProperties
val modAuthors: String by sharedProperties
val modDescription: String by sharedProperties
val modIssueTrackerUrl: String by sharedProperties
val modUpdateJsonUrl: String by sharedProperties
val modDisplayUrl: String by sharedProperties
val discord4jVersion: String by sharedProperties

val javaVersion: String by extra
val minecraftVersion: String by extra
val fabricLoaderVersion: String by extra
val fabricVersion: String by extra

version = modVersion
group = modGroup

base {
    archivesName = "${modId}-fabric-${modMinecraftVersion}"
}

loom {
    accessWidenerPath = file("src/main/resources/mc2discord.accesswidener")
}

repositories {
    maven("https://maven.parchmentmc.org")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://jitpack.io")
    mavenCentral()
}

configurations.implementation.get().extendsFrom(configurations.shadow.get())
dependencies {
    minecraft(group = "com.mojang", name = "minecraft", version = minecraftVersion)
    mappings(loom.officialMojangMappings())
    modImplementation(group = "net.fabricmc", name = "fabric-loader", version = fabricLoaderVersion)
    modImplementation(group = "net.fabricmc.fabric-api", name = "fabric-api", version = fabricVersion)

    shadow(project(":mc2discord-core"))

    @Suppress("VulnerableDependency")
    implementation(group = "io.netty", name = "netty-all", version = "4.1.68.Final") // Fix compatibility with Discord4J in dev

    compileOnly(group = "com.discord4j", name = "discord4j-core", version = discord4jVersion)
}

tasks {
    processResources {
        inputs.property("version", modVersion)

        filesMatching("fabric.mod.json") {
            expand("modId" to modId,
                    "modName" to modName,
                    "modGroup" to modGroup,
                    "modVersion" to modVersion,
                    "modMinecraftVersion" to modMinecraftVersion,
                    "modAuthors" to modAuthors,
                    "modDescription" to modDescription,
                    "modIssueTrackerUrl" to modIssueTrackerUrl,
                    "modUpdateJsonUrl" to modUpdateJsonUrl,
                    "modDisplayUrl" to modDisplayUrl,
                    "discord4jVersion" to discord4jVersion,

                    "javaVersion" to javaVersion,
                    "minecraftVersion" to minecraftVersion,
                    "fabricLoaderVersion" to fabricLoaderVersion,
                    "fabricVersion" to fabricVersion)
        }
    }

    java {
        sourceCompatibility = JavaVersion.toVersion(javaVersion)
        targetCompatibility = JavaVersion.toVersion(javaVersion)
    }

    jar {
        from("LICENSE") {
            rename { "${it}_${project.base.archivesName.get()}" }
        }
    }

    shadowJar {
        configurations = listOf(project.configurations.shadow.get())
        val relocateLocation = "$modGroup.shadow"
        val relocations = listOf("io.netty", "reactor", "discord4j", "org.reactivestreams", "org.checkerframework", "com.iwebpp.crypto", "com.google.errorprone", "com.github.benmanes.caffeine", "com.fasterxml.jackson", "com.discord4j.fsm", "com.austinv11.servicer", "com.vdurmont.emoji", "ml.denisd3d.config4j", "org.apache.commons.collections4", "org.immutables.encode", "org.json", "com.electronwill.nightconfig")
        relocations.forEach {
            relocate(it, "$relocateLocation.$it")
        }
        exclude("META-INF/services/**") // Fix compatibility with geckolib
    }

    remapJar {
        dependsOn(shadowJar)
        inputFile.set(shadowJar.get().archiveFile.get())
    }
}