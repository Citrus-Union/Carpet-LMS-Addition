plugins {
    alias(libs.plugins.spotless)
}

allprojects {
    group = providers.gradleProperty("maven_group").get()
    version = providers.gradleProperty("mod_version").get()

    repositories {
        maven("https://api.modrinth.com/maven")
        mavenCentral()

        // Add repositories to retrieve artifacts from in here.
        // You should only use this when depending on other mods because
        // Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
        // See https://docs.gradle.org/current/userguide/declaring_repositories.html
        // for more information about repositories.
    }
}

spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**", "**/.gradle/**")
        ktlint(libs.versions.ktlint.get())
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        targetExclude("**/build/**", "**/.gradle/**")
        ktlint(libs.versions.ktlint.get())
    }
    java {
        target("**/*.java")
        targetExclude("**/build/**", "**/.gradle/**")
        removeUnusedImports()
        forbidWildcardImports()
        forbidModuleImports()
        cleanthat()
        googleJavaFormat(libs.versions.googleJavaFormat.get())
            .reflowLongStrings()
            .formatJavadoc(false)
            .reorderImports(true)
    }
    format("styling") {
        target("**/*.md", "**/*.json", "**/*.yaml", "**/*.yml", "**/*.toml")
        targetExclude("**/build/**", "**/.gradle/**", "**/run/**")
        prettier(
            mapOf(
                "prettier" to libs.versions.prettier.get(),
                "prettier-plugin-toml" to libs.versions.prettierPluginToml.get(),
            ),
        ).config(
            mapOf(
                "plugins" to listOf("prettier-plugin-toml"),
            ),
        )
    }
}

tasks.register<Copy>("mergeLibs") {
    group = "build"
    subprojects.forEach { sub ->
        dependsOn(
            sub.tasks.matching {
                it.name == "jar" || it.name == "remapJar"
            },
        )
        from(sub.layout.buildDirectory.dir("libs"))
    }
    into(layout.buildDirectory.dir("libs"))
}

tasks.build {
    finalizedBy("mergeLibs")
}
