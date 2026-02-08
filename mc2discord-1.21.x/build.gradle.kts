import java.text.SimpleDateFormat
import java.util.*

plugins {
    id("java")
    id("idea")

    // Common
    id("org.spongepowered.gradle.vanilla") version ("0.2.2-SNAPSHOT") apply (false)
    id("com.gradleup.shadow") version ("9.3.1") apply (false)

    // Fabric
    id("net.fabricmc.fabric-loom-remap") version ("1.15-SNAPSHOT") apply (false)

    // Forge
    id("net.minecraftforge.gradle") version ("[7.0.2,8.0)") apply (false)
}

val sharedProperties = readProperties(file("../shared.properties"))
val modVersion: String = System.getenv("INPUT_VERSION") ?: "0.0.0-dev"

subprojects {
    version = modVersion
    group = sharedProperties["modGroup"]!!

    apply {
        plugin("java")
        plugin("idea")
    }

    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(rootProject.extra["javaVersion"] as String))
    }

    tasks.jar {
        manifest {
            attributes(
                "Specification-Title" to sharedProperties["modName"]!!,
                "Specification-Vendor" to sharedProperties["modAuthors"]!!,
                "Specification-Version" to modVersion,
                "Implementation-Title" to sharedProperties["modName"]!!,
                "Implementation-Vendor" to sharedProperties["modAuthors"]!!,
                "Implementation-Version" to modVersion,
                "Implementation-Timestamp" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date()),
                "Timestamp" to System.currentTimeMillis(),
                "Built-On-Java" to "${System.getProperty("java.vm.version")} (${System.getProperty("java.vm.vendor")})",
                "Build-On-Minecraft" to rootProject.extra["minecraftVersion"]!!
            )
        }
    }

    tasks.processResources {
        filesMatching(listOf("pack.mcmeta", "fabric.mod.json", "META-INF/mods.toml", "*.mixins.json")) {
            expand(
                rootProject.properties +
                        sharedProperties.map { it.key.toString() to it.value }.toMap() +
                        mapOf("modVersion" to modVersion)
            )
        }
    }
}
