import net.fabricmc.loom.task.AbstractRunTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.loom)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spotless)
    `maven-publish`
}

val cfgFile = file("version.toml")
val cfg = readToml(cfgFile)!!

val fabricApiModule = libs.fabricApi.get().module!!
val fabricApiVersion = cfg.getString("versions.fabric-api")
val minecraftModule = libs.minecraft.get().module!!
val minecraftVersion = cfg.getString("versions.minecraft")
val carpetModule = libs.carpet.get().module!!
val carpetVersion = cfg.getString("versions.carpet")

dependencies {
    minecraft("$minecraftModule:$minecraftVersion")
    implementation(libs.fabricLoader)
    implementation("$fabricApiModule:$fabricApiVersion")
    implementation(libs.fabricKotlin)
    implementation("$carpetModule:$carpetVersion")
    implementation(libs.snakeyaml)
    implementation(libs.gson)
    include(libs.snakeyaml)
    include(libs.gson)
}

tasks.named<ProcessResources>("processResources") {
    val name = providers.gradleProperty("mod_name").get()
    val description = providers.gradleProperty("mod_description").get()
    val fabricloaderDependency = providers.gradleProperty("fabricloader_dependency").get()
    val minecraftDependency = cfg.getString("dependency.minecraft")
    val fabricApiDependency = cfg.getString("dependency.fabric-api")
    val fabricLanguageKotlinDependency = providers.gradleProperty("fabric_language_kotlin_dependency").get()
    val carpetDependency = cfg.getString("dependency.carpet")
    val replaceMap =
        mapOf(
            "version" to version,
            "name" to name,
            "description" to description,
            "fabricloader" to fabricloaderDependency,
            "minecraft" to minecraftDependency,
            "fabric_api" to fabricApiDependency,
            "fabric_language_kotlin" to fabricLanguageKotlinDependency,
            "carpet" to carpetDependency,
        )
    inputs.properties(replaceMap)
    filesMatching("fabric.mod.json") {
        expand(replaceMap)
    }
}

tasks.withType<JavaCompile>().configureEach {
    javaCompiler.set(
        javaToolchains.compilerFor {
            languageVersion.set(JavaLanguageVersion.of(25))
        },
    )
    options.release.set(25)
    options.encoding = "UTF-8"
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_25)
    }
}

tasks.withType<AbstractRunTask>().configureEach {
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(25))
        },
    )
}

tasks.jar {
    val baseName = providers.gradleProperty("archives_base_name").get()
    val fileName = "$baseName-v$version-mc$minecraftVersion.jar"
    archiveFileName.set(fileName)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

kotlin {
    jvmToolchain(21)
}

sourceSets {
    named("main") {
        java.srcDir(rootProject.file("versionSrc/1.21.11+/src/main/java"))
        kotlin.srcDir(rootProject.file("versionSrc/1.21.11+/src/main/kotlin"))
        java.srcDir(rootProject.file("src/main/java"))
        kotlin.srcDir(rootProject.file("src/main/kotlin"))
        resources.srcDir(rootProject.file("src/main/resources"))
    }
}
