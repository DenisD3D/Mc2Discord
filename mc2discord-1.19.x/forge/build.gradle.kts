plugins {
    id("net.minecraftforge.gradle")
    id("org.spongepowered.mixin")
    id("com.github.johnrengelman.shadow")
}

val sharedProperties = readProperties(file("../../shared.properties"))

base {
    archivesName.set("${sharedProperties["modId"]}-forge-${rootProject.extra["minecraftDisplayVersion"]}")
}

mixin {
    add(sourceSets.main.get(), "${sharedProperties["modId"]}.refmap.json")

    config("${sharedProperties["modId"]}.mixins.json")
    config("${sharedProperties["modId"]}.forge.mixins.json")
    println("test")
}

repositories {
    maven("https://jitpack.io")
    mavenCentral()
}

val shadowMinecraftLibrary: Configuration by configurations.creating
val shadowCompileOnly: Configuration by configurations.creating
configurations.minecraftLibrary.get().extendsFrom(shadowMinecraftLibrary)
configurations.compileOnly.get().extendsFrom(shadowCompileOnly)
dependencies {
    minecraft("net.minecraftforge:forge:${rootProject.extra["minecraftVersion"]}-${rootProject.extra["forgeVersion"]}")
    shadowCompileOnly(project(":common"))
    shadowMinecraftLibrary(project(":mc2discord-core"))

    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
}

// Fix for running Discord4J on Forge in dev. Exclude forge netty and use the one from Discord4J
configurations.minecraft {
    exclude(group = "io.netty")
}


minecraft {
    mappings("official", "${rootProject.extra["minecraftVersion"]}")

    accessTransformer(file("src/main/resources/META-INF/accesstransformer.cfg"))

    runs {
        create("server") {
            taskName("Server")
            workingDirectory(project.file("run"))
            ideaModule("${rootProject.name}.${project.name}.main")
            arg("nogui")
            singleInstance(true)
            mods {
                create("${sharedProperties["modId"]}") {
                    source(sourceSets.main.get())
                    source(project(":common").sourceSets.main.get())
                }
            }
        }
    }
}

sourceSets.main.get().resources.srcDir("src/generated/resources")

tasks {
    withType<JavaCompile>().configureEach {
        source(project(":common").sourceSets.main.get().allSource)
    }

    processResources {
        from(project(":common").sourceSets.main.get().resources)
    }

    jar {
        archiveClassifier.set("slim")
        finalizedBy("reobfJar")
    }

    shadowJar {
        archiveClassifier.set("")
        configurations = listOf(shadowCompileOnly, shadowMinecraftLibrary)
        val relocateLocation = "${sharedProperties["modGroup"]}.shadow"
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
            "com.electronwill.nightconfig"
        )
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
}