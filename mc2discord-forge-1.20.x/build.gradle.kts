plugins {
    id("java")
    id("idea")
    id("net.minecraftforge.gradle") version ("5.1.+")
    id("org.parchmentmc.librarian.forgegradle") version ("1.+")
    id("com.github.johnrengelman.shadow") version ("7.1.2")
    id("org.spongepowered.mixin") version ("0.7.+")
}

val modId: String by extra
val modName: String by extra
val modGroup: String by extra
val modVersion: String = System.getenv("github.event.inputs.version") ?: "dev"
val modMinecraftVersion: String by extra
val modAuthors: String by extra
val modDescription: String by extra
val modIssueTrackerUrl: String by extra
val modUpdateJsonUrl: String by extra
val modDisplayUrl: String by extra

val javaVersion: String by extra
val forgeVersion: String by extra
val forgeVersionRange: String by extra
val minecraftVersion: String by extra
val minecraftVersionRange: String by extra
val parchmentMappingVersion: String by extra

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

    compileOnly(group = "com.discord4j", name = "discord4j-core", version = "3.3.0-M2")
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
        archiveBaseName.set("${modId}-forge-${modMinecraftVersion}-${modVersion}-forge")
        archiveClassifier.set("slim")
        manifest {
            attributes(mapOf("Specification-Title" to modName, "Specification-Vendor" to modAuthors, "Specification-Version" to "1", "Implementation-Title" to project.name, "Implementation-Version" to modVersion, "Implementation-Vendor" to modAuthors))
        }

    }

    shadowJar {
        archiveBaseName.set("${modId}-forge-${modMinecraftVersion}-${modVersion}")
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
    val replaceProperties = mapOf("modId" to modId, "modName" to modName, "modVersion" to modVersion, "modAuthors" to modAuthors, "modDescription" to modDescription, "modIssueTrackerUrl" to modIssueTrackerUrl, "modUpdateJsonUrl" to modUpdateJsonUrl, "modDisplayUrl" to modDisplayUrl, "minecraftVersionRange" to minecraftVersionRange, "forgeVersionRange" to forgeVersionRange)
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