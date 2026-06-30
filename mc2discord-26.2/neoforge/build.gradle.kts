plugins {
    id("java-library")
    id("net.neoforged.moddev")
    id("com.gradleup.shadow")
}

val sharedProperties = readProperties(file("../../shared.properties"))

base {
    archivesName.set("${sharedProperties["modId"]}-neoforge-${rootProject.extra["minecraftDisplayVersion"]}")
}

neoForge {
    version = rootProject.extra["neoforgeVersion"] as String

    // parchment {
    //     mappingsVersion = project.parchment_mappings_version
    //     minecraftVersion = project.parchment_minecraft_version
    // }

    // This line is optional. Access Transformers are automatically detected
    // accessTransformers = project.files('src/main/resources/META-INF/accesstransformer.cfg')

    runs {

        register("server") {
            server()
            programArgument("--nogui")
        }

        all {
            systemProperty("forge.logging.markers", "REGISTRIES")
            logLevel = org.slf4j.event.Level.INFO
        }
    }

    mods {
        create("${sharedProperties["modId"]}", Action {
            sourceSet(sourceSets.main.get())
        })
    }
}

repositories {
    maven("https://jitpack.io")
    mavenCentral()
}

val shade by configurations.creating
configurations.implementation.get().extendsFrom(shade)
dependencies {
    compileOnly(project(":common"))
    shade(project(":mc2discord-core"))
}

tasks {
    withType<JavaCompile>().configureEach {
        source(project(":common").sourceSets.main.get().allSource)
    }

    processResources {
        from(project(":common").sourceSets.main.get().resources)
    }

    jar {
        archiveClassifier.set("slim")   
    }

    shadowJar {
        archiveClassifier.set("")
        configurations = listOf(shade)
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
            "com.electronwill.nightconfig",
            "com.google.gson",
            "google.protobuf",
            "javax.annotation",
            "com.google.crypto.tink",
            "com.google.protobuf"
        )
        relocations.forEach {
            relocate(it, "$relocateLocation.$it")
        }
        exclude("META-INF/services/**") // Fix compatibility with geckolib
    }

    // Disable test tasks since we have no tests
    test {
        enabled = false
    }
    named<JavaCompile>("compileTestJava") {
        enabled = false
    }

    build {
        dependsOn(shadowJar)
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        from(jar.map { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.version}"))
        dependsOn("build")
    }
}