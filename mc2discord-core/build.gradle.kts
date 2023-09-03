plugins {
    id("java")
}

repositories {
    maven("https://jitpack.io")
    mavenCentral()
}

dependencies {
    // Discord4J
    implementation(group = "com.discord4j", name = "discord4j-core", version = "3.3.0-M2")

    // Config
    implementation(group = "com.github.denisd3d", name = "config4j", version = "1.1.6")
    implementation(group = "com.electronwill.night-config", name = "toml", version = "3.6.6")

    // Messages
    @Suppress("VulnerableDependency")
    implementation(group = "com.vdurmont", name = "emoji-java", version = "5.1.1")

    // Account
    implementation(group = "org.apache.commons", name = "commons-collections4", version = "4.4")

    // Minecraft dependencies (Shipped in Minecraft)
    compileOnly(group = "com.google.code.gson", name = "gson", version = "2.10")
    compileOnly(group = "commons-io", name = "commons-io", version = "2.11.0")
    compileOnly(group = "org.apache.commons", name = "commons-lang3", version = "3.11")
    compileOnly(group = "org.slf4j", name = "slf4j-api", version = "2.0.1")
    compileOnly(group = "com.google.guava", name = "guava", version = "31.1-jre")
    compileOnly(group = "org.apache.logging.log4j", name = "log4j-core", version = "2.19.0")

    // MISC
    compileOnly(group = "com.google.code.findbugs", name = "jsr305", version = "3.0.2") // Remove compilation warning
}