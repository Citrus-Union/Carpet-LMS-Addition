import org.tomlj.Toml

plugins {
    alias(libs.plugins.loom)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spotless)
    `maven-publish`
}

val cfgFile = file("version.toml")
val cfg = Toml.parse(cfgFile.readText())!!

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

sourceSets {
    named("main") {
        java.setSrcDirs(
            listOf(
                rootProject.file("src/main/java"),
                rootProject.file("versionSrc/1.21.11+/src/main/java"),
            ),
        )
        kotlin.setSrcDirs(
            listOf(
                rootProject.file("src/main/kotlin"),
                rootProject.file("versionSrc/1.21.11+/src/main/kotlin"),
            ),
        )
        resources.setSrcDirs(listOf(rootProject.file("src/main/resources")))
    }
}
