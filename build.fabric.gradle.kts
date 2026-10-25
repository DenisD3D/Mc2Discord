plugins {
    // Applies the remapping (<= 1.21.11) or non-remapping (26.1+) loom variant
    id("dev.kikugie.loom-back-compat")
    id("mc2discord-mod")
}

// Minecraft is not obfuscated anymore since 26.1
val obfuscated = sc.current.parsed < "26.1"

val modId = property("mod.id") as String

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
    loomx.applyMojangMappings()

    // loom-back-compat converts `mod*` configurations to the regular ones on 26.1+
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")
}

loom {
    accessWidenerPath = sc.process(
        rootProject.file("src/main/resources/$modId.accesswidener"),
        "build/processed.accesswidener",
    )

    runs {
        named("server") {
            ideConfigGenerated(true)
        }
    }
}

tasks {

    if (obfuscated) {
        // jar (-dev) -> shadowJar (-all) -> remapJar (final jar)
        named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
            dependsOn(shadowJar)
            inputFile.set(shadowJar.flatMap { it.archiveFile })
        }
    } else {
        // jar (-slim) -> shadowJar (final jar)
        jar {
            archiveClassifier.set("slim")
        }

        shadowJar {
            archiveClassifier.set("")
        }
    }
}
