rootProject.name = "mc2discord-fabric-1.19.x"

include(":mc2discord-core")
project(":mc2discord-core").projectDir = file("../mc2discord-core")

pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        mavenCentral()
        gradlePluginPortal()
    }
}