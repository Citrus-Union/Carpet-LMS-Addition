import com.github.gradle.node.NodeExtension
import com.github.gradle.node.pnpm.task.PnpmTask
import java.nio.file.Path

plugins {
    `maven-publish`
    signing
    id("net.fabricmc.fabric-loom") version "1.16-SNAPSHOT" apply false
    id("net.fabricmc.fabric-loom-remap") version "1.16-SNAPSHOT" apply false

    // https://github.com/ReplayMod/preprocessor
    // https://github.com/Fallen-Breath/preprocessor
    id("com.replaymod.preprocess") version "c5abb4fb12"

    // https://github.com/Fallen-Breath/yamlang
    id("me.fallenbreath.yamlang") version "1.5.0" apply false
    // https://github.com/diffplug/spotless/releases?q=gradle
    id("com.diffplug.spotless") version "8.5.1"
    // https://github.com/vanniktech/gradle-maven-publish-plugin
    id("com.vanniktech.maven.publish") version "0.36.0"
    // https://github.com/node-gradle/gradle-node-plugin
    id("com.github.node-gradle.node") version "7.1.0" apply false
}

repositories {
    mavenCentral()
}

apply(plugin = "com.github.node-gradle.node")

extensions.configure<NodeExtension> {
    download.set(true)
    version.set("24.16.0")
    pnpmVersion.set("11.4.0")
    nodeProjectDir.set(layout.projectDirectory.dir("storage-website").asFile)
}

val rootProjectRef: Project = project

preprocess {
    strictExtraMappings = false

    val mc1211 = createNode("1.21.1", 1_21_01, "")
    val mc1213 = createNode("1.21.3", 1_21_03, "")
    val mc1214 = createNode("1.21.4", 1_21_04, "")
    val mc1215 = createNode("1.21.5", 1_21_05, "")
    val mc1218 = createNode("1.21.8", 1_21_08, "")
    val mc12110 = createNode("1.21.10", 1_21_10, "")
    val mc12111 = createNode("1.21.11", 1_21_11, "")
    val mc260101 = createNode("26.1.2", 26_01_01, "")
    val mc260200 = createNode("26.2", 26_02_00, "")

    mc1211.link(mc1213, file("mappings/mapping-1.21.1-1.21.3.txt"))
    mc1213.link(mc1214, file("mappings/mapping-1.21.3-1.21.4.txt"))
    mc1214.link(mc1215)
    mc1215.link(mc1218)
    mc1218.link(mc12110)
    mc12110.link(mc12111, file("mappings/mapping-1.21.10-1.21.11.txt"))
    mc12111.link(mc260101, file("mappings/mapping-1.21.11-26.1.2.txt"))
    mc260101.link(mc260200, file("mappings/mapping-26.1.2-26.2.txt"))

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

val pnpmBuildStorageWebsite by tasks.registering(PnpmTask::class) {
    group = "build"
    description = "Builds the storage website via pnpm."
    dependsOn(tasks.named("pnpmInstall"))
    val frontendInputs =
        project.fileTree(file("storage-website")) {
            include("src/**")
            include("package.json", "pnpm-lock.yaml", "index.html", "vite.config.ts")
            include("tsconfig*.json", "env.d.ts", "eslint.config.ts")
            exclude("node_modules/**", "dist/**")
        }
    inputs.files(frontendInputs)
    outputs.dir(file("storage-website/dist"))
    pnpmCommand.set(listOf("build"))
}

val syncStorageWebsiteDist by tasks.registering(Sync::class) {
    group = "build"
    description = "Copies storage website dist files into resources."
    dependsOn(pnpmBuildStorageWebsite)
    from(layout.projectDirectory.dir("storage-website/dist"))
    into(layout.projectDirectory.dir("src/main/resources/websites/storage"))
}

val cleanStorageWebsiteResources by tasks.registering(Delete::class) {
    delete(layout.projectDirectory.dir("src/main/resources/websites/storage"))
}

tasks.named("clean") {
    dependsOn(cleanStorageWebsiteResources)
}

subprojects {
    tasks
        .matching { it.name == "processResources" || it.name == "preprocessResources" || it.name == "sourcesJar" }
        .configureEach {
            dependsOn(rootProject.tasks.named("syncStorageWebsiteDist"))
        }
}

spotless {
    val licenseHeaderFile = rootProject.file("copyright.txt")
    kotlinGradle {
        target("*.gradle.kts")
        ktlint(libs.versions.ktlint.get())
    }
    java {
        target("src/main/java/**/*.java", "versions/*/src/main/java/**/*.java")
        toggleOffOn()
        removeUnusedImports()
        forbidWildcardImports()
        forbidModuleImports()
        importOrderFile(rootProject.file("eclipse-importorder.txt"))
        cleanthat()
        val eclipseRelease = libs.versions.eclipse.get()
        val eclipseVersion = eclipseRelease.removePrefix("R-").substringBeforeLast("-")
        eclipse(eclipseVersion)
            .withP2Mirrors(
                mapOf(
                    "https://download.eclipse.org/eclipse/updates/$eclipseVersion/" to
                        "https://download.eclipse.org/eclipse/updates/$eclipseVersion/$eclipseRelease/",
                ),
            ).configFile(rootProject.file("eclipse-formatter.xml"))
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
            "website/public/*.html",
            "src/main/resources/*.json",
            "src/main/resources/assets/**/*.yml",
            "src/main/resources/assets/**/*.json",
            ".github/**/*.yml",
            "storage-website/src/**/*.tsx",
            "storage-website/src/**/*.ts",
            "storage-website/src/**/*.vue",
            "storage-website/src/**/*.json",
            "storage-website/src/**/*.css",
            "storage-website/*.json",
            "storage-website/*.html",
            "storage-website/*.ts",
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
            "versions/*/carpet-lms-addition.accesswidener",
            "copyright.txt",
            "mappings/*.txt",
            "website/public/_headers",
            "eclipse-importorder.txt",
            "storage-website/src/i18n/mojang/version.txt",
        )
        trimTrailingWhitespace()
        endWithNewline()
    }
}
