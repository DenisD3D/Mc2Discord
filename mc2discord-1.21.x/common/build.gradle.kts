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
    accessWideners(file("src/main/resources/${sharedProperties["modId"]}.accesswidener"))
}

repositories {
    maven("https://jitpack.io")
    mavenCentral()
}

dependencies {
    compileOnly(group = "org.spongepowered", name = "mixin", version = "0.8.5")
    api(project(":mc2discord-core"))
}