import java.nio.file.Path

plugins {
    `maven-publish`
    signing
    id("net.fabricmc.fabric-loom") version "1.15-SNAPSHOT" apply false
    id("net.fabricmc.fabric-loom-remap") version "1.15-SNAPSHOT" apply false

    // https://github.com/ReplayMod/preprocessor
    // https://github.com/Fallen-Breath/preprocessor
    id("com.replaymod.preprocess") version "c5abb4fb12"

    // https://github.com/Fallen-Breath/yamlang
    id("me.fallenbreath.yamlang") version "1.5.0" apply false
    id("com.diffplug.spotless") version "8.2.1"
    id("com.vanniktech.maven.publish") version "0.36.0"
}

repositories {
    mavenCentral()
}

val rootProjectRef: Project = project

preprocess {
    strictExtraMappings = false

    val mc12110 = createNode("1.21.10", 1_21_10, "")
    val mc12111 = createNode("1.21.11", 1_21_11, "")
    val mc260100 = createNode("26.1", 26_01_00, "")

    mc12110.link(mc12111, file("mappings/mapping-1.21.10-1.21.11.txt"))
    mc12111.link(mc260100, file("mappings/mapping-1.21.11-26.1.txt"))

    // See https://github.com/Fallen-Breath/fabric-mod-template/blob/1d72d77a1c5ce0bf060c2501270298a12adab679/build.gradle#L55-L63
    for (node in getNodes()) {
        val nodeProject =
            requireNotNull(rootProjectRef.findProject(node.project)) {
                "Project ${node.project} not found"
            }
        nodeProject.extensions.extraProperties["mcVersion"] = node.mcVersion
    }
}

tasks.register("buildAndGather") {
    group = "build"
    dependsOn(project.subprojects.map { it.tasks.named("build") })
    doFirst {
        println("Gathering builds")
        val buildLibs: (Project) -> Path = { p ->
            p.layout.buildDirectory
                .dir("libs")
                .get()
                .asFile
                .toPath()
        }
        project.delete(project.fileTree(buildLibs(rootProject)) { include("*") })
        project.subprojects.forEach { subproject ->
            project.copy {
                from(buildLibs(subproject)) {
                    include("*.jar")
                    exclude("*-dev.jar", "*-sources.jar", "*-shadow.jar", "*-javadoc.jar")
                }
                into(buildLibs(rootProject))
                duplicatesStrategy = DuplicatesStrategy.INCLUDE
            }
        }
    }
}

spotless {
    val licenseHeaderFile = rootProject.file("copyright.txt")
    kotlinGradle {
        target("*.gradle.kts")
        ktlint(libs.versions.ktlint.get())
    }
    java {
        target("src/**/*.java", "versions/*/src/**/*.java")
        removeUnusedImports()
        forbidWildcardImports()
        forbidModuleImports()
        importOrder(
            "java",
            "javax",
            "net.minecraft",
            "com.mojang",
            "net.fabricmc",
            "org.spongepowered",
            "com.llamalad7",
            "carpet",
            "org",
            "com",
            "net",
            "cn.nm.lms",
            "",
        )
        cleanthat()
        eclipse(libs.versions.eclipse.get()).configFile(rootProject.file("eclipse-formatter.xml"))
        licenseHeaderFile(licenseHeaderFile)
    }
    format("styling") {
        target(
            "gradle/libs.versions.toml",
            "*.md",
            "*.json",
            "*.yml",
            "*.xml",
            "website/src/**/*.md",
            "website/src/**/*.mdx",
            "website/src/**/*.astro",
            "website/src/**/*.ts",
            "website/*.json",
            "website/*.yaml",
            "website/*.mjs",
            "src/main/resources/**/*.json",
            "src/main/resources/**/*.yml",
            ".github/**/*.yml",
        )

        prettier(
            mapOf(
                "prettier" to libs.versions.prettier.get(),
                "prettier-plugin-toml" to
                    libs.versions.prettierPlugin.toml
                        .get(),
                "@prettier/plugin-xml" to
                    libs.versions.prettierPlugin.xml
                        .get(),
                "prettier-plugin-astro" to
                    libs.versions.prettierPlugin.astro
                        .get(),
            ),
        ).config(
            mapOf(
                "plugins" to
                    listOf(
                        "prettier-plugin-toml",
                        "@prettier/plugin-xml",
                        "prettier-plugin-astro",
                    ),
            ),
        )
    }
    format("text") {
        target(
            "LICENSE",
            "gradle.properties",
            "gradle/wrapper/gradle-wrapper.properties",
            "versions/*/gradle.properties",
            "copyright.txt",
            "mappings/*.txt",
            "website/public/robots.txt",
        )
        trimTrailingWhitespace()
        endWithNewline()
    }
}
