import com.vanniktech.maven.publish.MavenPublishBaseExtension
import net.fabricmc.loom.api.LoomGradleExtensionAPI

val mcVersion = (project.extra["mcVersion"] as Number).toInt()
val unobfuscated = mcVersion >= 26_00_00

pluginManager.apply(if (unobfuscated) "net.fabricmc.fabric-loom" else "net.fabricmc.fabric-loom-remap")
pluginManager.apply("com.replaymod.preprocess")
pluginManager.apply("me.fallenbreath.yamlang")
pluginManager.apply("com.vanniktech.maven.publish")

fun gradleProperty(name: String): String = project.findProperty(name).toString()

val minecraftVersion = gradleProperty("minecraftVersion")
val parchmentVersion = gradleProperty("parchmentVersion")
val carpetVersion = gradleProperty("carpetVersion")
val modVersion = gradleProperty("modVersion")
val modId = gradleProperty("modId")
val modName = gradleProperty("modName")
val modDescription = gradleProperty("modDescription")
val modSource = gradleProperty("modSource")
val modWebsite = gradleProperty("modWebsite")
val minecraftDependency = gradleProperty("minecraftDependency")
val fabricloaderDependency = gradleProperty("fabricloaderDependency")
val mavenGroup = gradleProperty("mavenGroup")
val archivesBaseName = gradleProperty("archivesBaseName")
val modrinthUrl = gradleProperty("modrinthUrl")
val curseforgeUrl = gradleProperty("curseforgeUrl")
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

    fun includeDependency(dep: Any?): Dependency? = processDependency(add("include", requireNotNull(dep)))

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
    autoImplementation(libs.fabric.loader.get())
    autoImplementation("carpet:fabric-carpet:$carpetVersion")

    // https://central.sonatype.com/artifact/org.jspecify/jspecify
    autoCompileOnly(libs.jspecify.get())
    // runtime mods
    autoRuntimeOnly("me.fallenbreath:mixin-auditor:0.2.0-${if (unobfuscated) "u" else "o"}")

    includeDependency(autoImplementation(libs.conditional.mixin.get()))

    includeDependency(autoImplementation(libs.jbcrypt.get()))
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

val commonVmArgs = listOf("--sun-misc-unsafe-memory-access=allow", "-Dmixin.debug.export=true")
loomExtension.runConfigs.configureEach {
    runDirectory.set(
        file(
            if (unobfuscated) "../../run" else "../../run-obsuscated",
        ),
    )
    jvmArguments.addAll(commonVmArgs)
}
loomExtension.runs {
    val auditVmArgs = "-DmixinAuditor.audit=true"
    create("serverMixinAudit") {
        server()
        jvmArguments.add(auditVmArgs)
    }
    create("clientMixinAudit") {
        client()
        jvmArguments.add(auditVmArgs)
    }
}

tasks.matching { it.name.startsWith("run") }.configureEach {
    dependsOn(rootProject.subprojects.map { subproject -> subproject.tasks.matching { it.name == "downloadAssets" } })
}

loomExtension.accessWidenerPath.set(file("carpet-lms-addition.accesswidener"))

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
        "website" to modWebsite,
        "issues" to issueTrackerUrl,
        "modrinth" to modrinthUrl,
        "curseforge" to curseforgeUrl,
        "minecraft_dependency" to minecraftDependency,
        "fabricloader_dependency" to fabricloaderDependency,
    )

tasks.named<ProcessResources>("processResources") {
    from(project.file("carpet-lms-addition.accesswidener"))
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
val targetParamType: Class<*> = targetSourceSetsSetter.parameterTypes[0]
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
val inputParamType: Class<*> = inputDirSetter.parameterTypes[0]
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
        url.set(modWebsite)
        licenses {
            license {
                name.set("The GNU General Public License v3.0")
                url.set("https://www.gnu.org/licenses/gpl-3.0.html")
            }
        }
        developers {
            developer {
                id.set("Bdbmzwsc")
                name.set("bdbmzwsc")
                email.set("chinabdbmzwsc@outlook.com")
                url.set("https://github.com/Bdbmzwsc")
            }
            developer {
                id.set("jasonxue1")
                name.set("jasonxue")
                email.set("hi@jasonxue.dev")
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
