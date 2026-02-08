plugins {
    id("net.minecraftforge.gradle")
    id("com.gradleup.shadow")
}

val sharedProperties = readProperties(file("../../shared.properties"))

base {
    archivesName.set("${sharedProperties["modId"]}-forge-${rootProject.extra["minecraftDisplayVersion"]}")
}

repositories {
    minecraft.mavenizer(this)
    maven(fg.forgeMaven)
    maven(fg.minecraftLibsMaven)
    exclusiveContent {
        forRepository {
            maven {
                name = "Sponge"
                url = uri("https://repo.spongepowered.org/repository/maven-public")
            }
        }
        filter {
            includeGroupAndSubgroups("org.spongepowered")
        }
    }
    maven("https://jitpack.io")
    mavenCentral()
    mavenLocal()
}

val shadowMinecraftLibrary: Configuration by configurations.creating
val shadowCompileOnly: Configuration by configurations.creating
configurations.compileOnly.get().extendsFrom(shadowCompileOnly)
configurations.implementation.get().extendsFrom(shadowMinecraftLibrary)

dependencies {
    implementation(minecraft.dependency("net.minecraftforge:forge:${rootProject.extra["minecraftVersion"]}-${rootProject.extra["forgeVersion"]}"))
    shadowCompileOnly(project(":common"))
    shadowMinecraftLibrary(project(":mc2discord-core"))

    annotationProcessor("net.minecraftforge:eventbus-validator:7.0-beta.10")


    implementation("net.sf.jopt-simple:jopt-simple:5.0.4") {
        version {
            strictly("5.0.4")
        }
    }
}

minecraft {
    mappings("official", "${rootProject.extra["minecraftVersion"]}")
    useDefaultAccessTransformer()

    runs {
        register("server") {
            // taskName("Server")
            // workingDirectory(project.file("run"))
            // ideaModule("${rootProject.name}.${project.name}.main")
            // arg("nogui")
            // singleInstance(true)
            // mods {
            //     create("${sharedProperties["modId"]}") {
            //         source(sourceSets.main.get())
            //         source(project(":common").sourceSets.main.get())
            //     }
            // }
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
        
        manifest.attributes(
            "MixinConfigs" to "${sharedProperties["modId"]}.mixins.json"
        )
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

    test {
        enabled = false
    }
}