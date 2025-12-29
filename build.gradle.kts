import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.tomlj.Toml

buildscript {
    dependencies {
        classpath(libs.tomlj)
    }
}

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.spotless)
}

allprojects {
    group = providers.gradleProperty("maven_group").get()
    version = providers.gradleProperty("mod_version").get()

    repositories {
        maven("https://api.modrinth.com/maven")
        mavenCentral()
    }

    plugins.withId("java") {
        extensions.configure<JavaPluginExtension>("java") {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(25))
                vendor.set(JvmVendorSpec.AZUL)
            }
        }
    }
}

subprojects {

    if (!tasks.names.contains("prepareKotlinBuildScriptModel")) {
        tasks.register("prepareKotlinBuildScriptModel")
    }

    val cfgFile = file("version.toml")

    val cfg = Toml.parse(cfgFile.readText())!!
    val minecraftVersion = cfg.getString("versions.minecraft")

    tasks.withType<ProcessResources>().configureEach {
        val name = providers.gradleProperty("mod_name").get()
        val description = providers.gradleProperty("mod_description").get()
        val source = providers.gradleProperty("mod_source").get()
        val fabricloaderDependency = providers.gradleProperty("fabricloader_dependency").get()
        val minecraftDependency = cfg.getString("dependency.minecraft")
        val fabricApiDependency = cfg.getString("dependency.fabric-api")
        val fabricLanguageKotlinDependency =
            providers.gradleProperty("fabric_language_kotlin_dependency").get()
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
                "source" to source,
            )
        inputs.properties(replaceMap)
        filesMatching("fabric.mod.json") {
            expand(replaceMap)
        }
    }

    tasks.matching { it.name == "jar" || it.name == "remapJar" }.configureEach {
        val baseName = providers.gradleProperty("archives_base_name").get()
        val fileName = "$baseName-v$version-mc$minecraftVersion.jar"
        (this as AbstractArchiveTask).archiveFileName.set(fileName)
    }

    plugins.withId("java") {
        tasks.withType<JavaCompile>().configureEach {
            options.release.set(25)
            options.encoding = "UTF-8"
        }
    }

    plugins.withId("org.jetbrains.kotlin.jvm") {
        tasks.withType<KotlinCompile>().configureEach {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_25)
            }
        }
        extensions.configure<KotlinJvmProjectExtension>("kotlin") {
            jvmToolchain(25)
        }
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
