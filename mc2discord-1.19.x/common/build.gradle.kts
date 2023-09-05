plugins {
    id("java-library")
    id("org.spongepowered.gradle.vanilla")
}

val sharedProperties = readProperties(file("../../shared.properties"))

base {
    archivesName.set("${sharedProperties["modId"]}-common-${rootProject.extra["minecraftDisplayVersion"]}")
}

minecraft {
    version(rootProject.extra["minecraftVersion"] as String)
    if (file("src/main/resources/${sharedProperties["modName"]}.accesswidener").exists()) {
        accessWideners(file("src/main/resources/${sharedProperties["modName"]}.accesswidener"))
    }
}

repositories {
    maven("https://jitpack.io")
    mavenCentral()
}

dependencies {
    compileOnly(group = "org.spongepowered", name = "mixin", version = "0.8.5")
    implementation(project(":mc2discord-core"))
}