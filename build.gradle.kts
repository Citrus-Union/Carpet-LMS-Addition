import net.fabricmc.loom.task.RemapJarTask
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.loom)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spotless)
    `maven-publish`
}

version = libs.versions.mod.get()
group = providers.gradleProperty("maven_group").get()

repositories {
    maven("https://api.modrinth.com/maven")
    mavenCentral()

    // Add repositories to retrieve artifacts from in here.
    // You should only use this when depending on other mods because
    // Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
    // See https://docs.gradle.org/current/userguide/declaring_repositories.html
    // for more information about repositories.
}

val embeddedLibs: Configuration by configurations.creating

dependencies {
    minecraft(libs.minecraft)
    mappings(libs.yarn)
    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.api)
    modImplementation(libs.fabric.kotlin)
    modImplementation(libs.carpet)
    implementation(libs.snakeyaml)
    implementation(libs.gson)
    embeddedLibs(libs.snakeyaml)
    embeddedLibs(libs.gson)
}

tasks.named<ProcessResources>("processResources") {
    val name = providers.gradleProperty("mod_name").get()
    val description = providers.gradleProperty("mod_description").get()
    inputs.property("version", version)
    inputs.property("ame", name)
    inputs.property("description", description)
    filesMatching("fabric.mod.json") {
        expand(
            mapOf(
                "version" to version,
                "name" to name,
                "description" to description,
            ),
        )
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
    options.encoding = "UTF-8"
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

tasks.named<RemapJarTask>("remapJar") {
    val mcVersion = libs.versions.minecraft.get()
    val baseName = providers.gradleProperty("archives_base_name").get()
    val fileName = "$baseName-v$version-mc$mcVersion.jar"
    archiveFileName.set(fileName)
}

tasks.named<Jar>("jar") {
    from(
        embeddedLibs.files.map { file ->
            if (file.isDirectory) file else zipTree(file)
        },
    )
    exclude("META-INF/**")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

kotlin {
    jvmToolchain(21)
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
