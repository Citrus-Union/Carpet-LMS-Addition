import com.vanniktech.maven.publish.MavenPublishBaseExtension
import net.fabricmc.loom.api.LoomGradleExtensionAPI

val mcVersion = (project.extra["mcVersion"] as Number).toInt()
val unobfuscated = mcVersion >= 26_00_00

apply(plugin = if (unobfuscated) "net.fabricmc.fabric-loom" else "net.fabricmc.fabric-loom-remap")
apply(plugin = "com.replaymod.preprocess")
apply(plugin = "me.fallenbreath.yamlang")
apply(plugin = "com.vanniktech.maven.publish")

val minecraftVersion: String by project
val parchmentVersion: String by project
val loaderVersion: String by project
val fabricApiVersion: String by project
val carpetVersion: String by project
val modVersion: String by project
val modId: String by project
val modName: String by project
val modDescription: String by project
val modSource: String by project
val modWiki: String by project
val minecraftDependency: String by project
val fabricloaderDependency: String by project
val mavenGroup: String by project
val archivesBaseName: String by project
val issueTrackerUrl: String = "$modSource/issues"

repositories {
    maven {
        url = uri("https://maven.parchmentmc.org")
        content { includeGroup("org.parchmentmc.data") }
    }
    maven {
        url = uri("https://jitpack.io")
        content {
            includeGroup("com.github")
            includeGroupByRegex("com\\.github\\..+")
        }
    }
    maven {
        url = uri("https://maven.fallenbreath.me/releases")
        content { includeGroup("me.fallenbreath") }
    }
    maven {
        url = uri("https://masa.dy.fi/maven")
        content { includeGroup("carpet") }
    }
}

val loomExtension = extensions.getByType(LoomGradleExtensionAPI::class)

dependencies {
    fun processDependency(dep: Dependency?): Dependency? {
        // https://github.com/FabricMC/fabric-loader/issues/783
        if (dep is ModuleDependency && !(dep.group == "net.fabricmc" && dep.name == "fabric-loader")) {
            dep.exclude(mapOf("group" to "net.fabricmc", "module" to "fabric-loader"))
        }
        return dep
    }

    fun autoImplementation(dep: Any): Dependency? = processDependency(add(if (unobfuscated) "implementation" else "modImplementation", dep))

    fun autoRuntimeOnly(dep: Any): Dependency? = processDependency(add(if (unobfuscated) "runtimeOnly" else "modRuntimeOnly", dep))

    fun autoCompileOnly(dep: Any): Dependency? = processDependency(add(if (unobfuscated) "compileOnly" else "modCompileOnly", dep))

    // loom
    add("minecraft", "com.mojang:minecraft:$minecraftVersion")
    if (!unobfuscated) {
        @Suppress("UnstableApiUsage")
        add(
            "mappings",
            loomExtension.layered {
                officialMojangMappings()
                if (parchmentVersion.isNotEmpty()) {
                    parchment("org.parchmentmc.data:parchment-$minecraftVersion:$parchmentVersion@zip")
                }
            },
        )
    }
    autoImplementation("net.fabricmc:fabric-loader:$loaderVersion")
    autoImplementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
    autoImplementation("carpet:fabric-carpet:$carpetVersion")

    autoCompileOnly("org.jspecify:jspecify:1.0.0")
    // runtime mods
//  if (mcVersion < 11904) {
//      autoRuntimeOnly(mcVersion < 11900 ? "com.github.astei:lazydfu:0.1.2" : "com.github.Fallen-Breath:lazydfu:a7cfc44c0c")
//  }
    autoRuntimeOnly("me.fallenbreath:mixin-auditor:0.2.0-${if (unobfuscated) "u" else "o"}")

    // dependencies
//  val fabricApiModules = mutableListOf<String>()
//  if (mcVersion >= 12105) {  // "fabric-resource-loader-v0" dependency since fabric api 0.117.0 (25w07a, mc1.21.5 snapshot)
//      fabricApiModules.add("fabric-api-base")
//  }
//  if (mcVersion < 12111) {  // "fabric-resource-loader" is fully moved from v0 to v1 since fabric api 0.139.3 (1.21.11-pre4)
//      fabricApiModules.add("fabric-resource-loader-v0")
//  }
//  if (mcVersion >= 12109) {  // "fabric-resource-loader-v0" dependency since fabric api 0.133.11 (1.21.9-rc1)
//      fabricApiModules.add("fabric-resource-loader-v1")
//  }
//  fabricApiModules.forEach {
//      include(autoImplementation(fabricApi.module(it, fabric_api_version)))
//  }
//  include(autoImplementation("me.fallenbreath:conditional-mixin-fabric:$conditionalmixin_version"))
//  if (mcVersion < 12005) {
//      include("io.github.llamalad7:mixinextras-fabric:$mixinextras_version")
//  }
}

val mixinConfigPath = "carpet-lms-addition.mixins.json"
val langDir = "assets/carpetlmsaddition/lang"
val javaCompatibility =
    when {
        mcVersion >= 26_00_00 -> JavaVersion.VERSION_25
        mcVersion >= 12005 -> JavaVersion.VERSION_21
        mcVersion >= 11800 -> JavaVersion.VERSION_17
        mcVersion >= 11700 -> JavaVersion.VERSION_16
        else -> JavaVersion.VERSION_1_8
    }
val mixinCompatibilityLevel = javaCompatibility

val commonVmArgs = listOf("-Dmixin.debug.export=true", "-Dmixin.debug.countInjections=true")
loomExtension.runConfigs.configureEach {
    // to make sure it generates all "Minecraft Client (:subproject_name)" applications
    isIdeConfigGenerated = true
    runDir = "../../run"
    vmArgs(commonVmArgs)
}
loomExtension.runs {
    val auditVmArgs = commonVmArgs + "-DmixinAuditor.audit=true"
    register("serverMixinAudit") {
        server()
        vmArgs.addAll(auditVmArgs)
        isIdeConfigGenerated = false
    }
    register("clientMixinAudit") {
        client()
        vmArgs.addAll(auditVmArgs)
        isIdeConfigGenerated = false
    }
}

var modVersionSuffix = ""
val artifactVersion = modVersion
var artifactVersionSuffix = ""
// detect github action environment variables
// https://docs.github.com/en/actions/learn-github-actions/environment-variables#default-environment-variables
if (System.getenv("BUILD_RELEASE") != "true") {
    val buildNumber = System.getenv("BUILD_ID")
    modVersionSuffix += if (buildNumber != null) "+build.$buildNumber" else "-SNAPSHOT"
    artifactVersionSuffix = "-SNAPSHOT" // A non-release artifact is always a SNAPSHOT artifact
}
val fullModVersion = modVersion + modVersionSuffix
var fullProjectVersion = ""
var fullArtifactVersion = ""

// Example version values:
//   project.mod_version     1.0.3                      (the base mod version)
//   modVersionSuffix        +build.88                  (use github action build number if possible)
//   artifactVersionSuffix   -SNAPSHOT
//   fullModVersion          1.0.3+build.88             (the actual mod version to use in the mod)
//   fullProjectVersion      v1.0.3-mc1.15.2+build.88   (in build output jar name)
//   fullArtifactVersion     1.0.3-mc1.15.2-SNAPSHOT    (maven artifact version)

group = mavenGroup
val baseExtension = extensions.getByType(BasePluginExtension::class)
if (System.getenv("JITPACK") == "true") {
    // move mc version into archivesBaseName, so jitpack will be able to organize archives from multiple subprojects correctly
    baseExtension.archivesName.set("$archivesBaseName-mc$minecraftVersion")
    fullProjectVersion = "v$modVersion$modVersionSuffix"
    fullArtifactVersion = artifactVersion + artifactVersionSuffix
} else {
    baseExtension.archivesName.set(archivesBaseName)
    fullProjectVersion = "v$modVersion-mc$minecraftVersion$modVersionSuffix"
    fullArtifactVersion = "$artifactVersion-mc$minecraftVersion$artifactVersionSuffix"
}
version = fullProjectVersion

// See https://youtrack.jetbrains.com/issue/IDEA-296490
// if IDEA complains about "Cannot resolve resource filtering of MatchingCopyAction" and you want to know why
val modProperties =
    mapOf(
        "id" to modId,
        "name" to modName,
        "version" to fullModVersion,
        "description" to modDescription,
        "source" to modSource,
        "wiki" to modWiki,
        "issues" to issueTrackerUrl,
        "minecraft_dependency" to minecraftDependency,
        "fabricloader_dependency" to fabricloaderDependency,
    )

tasks.named<ProcessResources>("processResources") {
    inputs.properties(modProperties)

    filesMatching("fabric.mod.json") {
        expand(modProperties)
    }

    filesMatching(mixinConfigPath) {
        filter { line: String ->
            line.replace("{{COMPATIBILITY_LEVEL}}", "JAVA_${mixinCompatibilityLevel.ordinal + 1}")
        }
    }
}

// https://github.com/Fallen-Breath/yamlang
val mainSourceSet = extensions.getByType(SourceSetContainer::class).getByName("main")
val yamlangExtension = extensions.getByName("yamlang")
val targetSourceSetsSetter =
    yamlangExtension.javaClass.methods.firstOrNull {
        it.name == "setTargetSourceSets" && it.parameterCount == 1
    } ?: error("yamlang extension does not expose setTargetSourceSets")
val targetParamType = targetSourceSetsSetter.parameterTypes[0]
val targetSourceSetsValue: Any =
    when {
        targetParamType.isAssignableFrom(Set::class.java) -> setOf(mainSourceSet)
        targetParamType.isAssignableFrom(List::class.java) -> listOf(mainSourceSet)
        targetParamType.isAssignableFrom(Collection::class.java) -> listOf(mainSourceSet)
        targetParamType.isAssignableFrom(Iterable::class.java) -> listOf(mainSourceSet)
        else -> listOf(mainSourceSet)
    }
targetSourceSetsSetter.invoke(yamlangExtension, targetSourceSetsValue)
val inputDirSetter =
    yamlangExtension.javaClass.methods.firstOrNull {
        it.name == "setInputDir" && it.parameterCount == 1
    } ?: error("yamlang extension does not expose setInputDir")
val inputParamType = inputDirSetter.parameterTypes[0]
val inputDirValue: Any = if (inputParamType == File::class.java) file(langDir) else langDir
inputDirSetter.invoke(yamlangExtension, inputDirValue)

// ensure that the encoding is set to UTF-8, no matter what the system default is
// this fixes some edge cases with special characters not displaying correctly
// see http://yodaconditions.net/blog/fix-for-java-file-encoding-problems-with-gradle.html
tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-Xlint:deprecation", "-Xlint:unchecked"))
    if (javaCompatibility <= JavaVersion.VERSION_1_8) {
        // suppressed "source/target value 8 is obsolete and will be removed in a future release"
        options.compilerArgs.add("-Xlint:-options")
    }
}

extensions.getByType(JavaPluginExtension::class).apply {
    sourceCompatibility = javaCompatibility
    targetCompatibility = javaCompatibility

    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()
}

tasks.named<Jar>("jar") {
    inputs.property("archives_base_name", archivesBaseName)
    from(rootProject.file("LICENSE")) {
        rename { name -> "${name}_${inputs.properties["archives_base_name"]}" }
    }
}

extensions.configure<MavenPublishBaseExtension>("mavenPublishing") {
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()
    coordinates(
        groupId = group.toString(),
        artifactId = baseExtension.archivesName.get(),
        version = fullArtifactVersion,
    )

    pom {
        name.set(modName)
        description.set(modDescription)
        url.set(modWiki)
        licenses {
            license {
                name.set("The GNU General Public License v3.0")
                url.set("https://www.gnu.org/licenses/gpl-3.0.html")
            }
        }
        developers {
            developer {
                id.set("jasonxue1")
                name.set("jasonxue")
                email.set("lms@jasonxue.top")
                url.set("https://github.com/jasonxue1")
            }
            developer {
                id.set("LittleLemonJam")
                name.set("小柠檬lemon酱")
                url.set("https://github.com/LittleLemonJam")
            }
            developer {
                id.set("stupidOIO")
                name.set("stupidOIO")
                url.set("https://github.com/stupidOIO")
            }
        }
        scm {
            url.set(modSource)
            connection.set("scm:git:$modSource.git")

            val sshUrl =
                modSource
                    .removePrefix("https://")
                    .let { "ssh://git@$it.git" }

            developerConnection.set("scm:git:$sshUrl")
        }
        issueManagement {
            system = "Github Issues"
            url = issueTrackerUrl
        }
    }
}
