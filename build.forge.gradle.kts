plugins {
    id("net.minecraftforge.gradle") version "[7.0.2,8.0)"
    id("mc2discord-mod")
}

val modId = property("mod.id") as String

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
}

dependencies {
    implementation(minecraft.dependency("net.minecraftforge:forge:${sc.current.version}-${property("deps.forge")}"))

    // EventBus 7 is used since Forge 1.21.6
    if (sc.current.parsed >= "1.21.6") {
        annotationProcessor("net.minecraftforge:eventbus-validator:7.0-beta.10")
    }

    implementation("net.sf.jopt-simple:jopt-simple:5.0.4") {
        version {
            strictly("5.0.4")
        }
    }
}

minecraft {
    mappings("official", sc.current.version)
    accessTransformer.from(rootProject.file("src/main/resources/META-INF/accesstransformer.cfg"))

    runs {
        register("server") {
        }
    }
}

tasks {
    jar {
        archiveClassifier.set("slim")

        manifest.attributes(
            "MixinConfigs" to "$modId.mixins.json"
        )
    }

    shadowJar {
        archiveClassifier.set("")
    }
}
