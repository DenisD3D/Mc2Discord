plugins {
    id("dev.kikugie.stonecutter")
    id("com.diffplug.spotless") version "8.0.0"
}

repositories {
    mavenCentral()
}

// Java formatting (./gradlew spotlessApply), rules in eclipse-formatter.xml, also used by VS Code (.vscode/settings.json).
// Only formats the code of the active version: the other versions are in comments, which are left untouched.
spotless {
    java {
        target("src/*/java/**/*.java", "mc2discord-core/src/*/java/**/*.java")
        eclipse().configFile("eclipse-formatter.xml")
    }

    // Gradle scripts, rules in .editorconfig
    kotlinGradle {
        target("*.gradle.kts", "mc2discord-core/*.gradle.kts", "buildSrc/*.gradle.kts", "buildSrc/src/main/kotlin/*.gradle.kts")
        ktlint()
    }

    json {
        target("*.json", "src/*/resources/**/*.json", "src/*/resources/**/*.mcmeta", "mc2discord-core/src/*/resources/**/*.json")
        gson().indentWithSpaces(4)
    }
}

stonecutter active "26.3-fabric"

stonecutter parameters {
    val (minecraft, loader) = current.project.split('-', limit = 2)

    // Applies the version and loader specific tables of `stonecutter.properties.toml`
    properties {
        tags(minecraft, loader)
    }

    // Usable in comments as `//? if fabric {`, `//? if forge {` and `//? if neoforge {`
    constants {
        match(loader, "fabric", "forge", "neoforge")
    }

    // Renames between Minecraft versions: the code in `src/` always uses the newest name,
    // it is replaced by the old one when building a Minecraft version older than `since`.
    replacements {
        fun renamed(since: String, old: String, new: String) = string(current.parsed >= since) { replace(old, new) }

        renamed("1.21.9", old = "getGameProfile().getName()", new = "getGameProfile().name()")
        renamed("1.21.9", old = "getGameProfile().getId()", new = "getGameProfile().id()")
        renamed("26.3", old = ".shouldAnnounceChat()", new = ".announceToChat()")
        renamed("26.3", old = "display().get().getTitle()", new = "display().get().title()")
        renamed("26.3", old = "display().get().getDescription()", new = "display().get().description()")
        renamed("26.1", old = "accessWidener v1 named", new = "accessWidener v1 official")
    }
}
