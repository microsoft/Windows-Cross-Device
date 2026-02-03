buildscript {
    dependencies {
        classpath(libs.android.gradle.plugin)
        classpath(libs.ktlint.gradle)
        classpath(libs.kotlin.gradle.plugin)
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.dokka)
    alias(libs.plugins.detekt)
}

fun getCustomVersionCode(): Int {
    if (project.hasProperty("BuildServer")) {
        println("Building on build server")
    }
    val ts = System.currentTimeMillis() / 1000L - 1504000000L
    return if (ts > 0) {
        val code = (ts / (30 * 60L)).toInt() // round to 30 minutes periods
        println("Auto increase version code to: $code")
        code
    } else {
        0
    }
}

fun getGitDescription(): String {
    try {
        val process = Runtime.getRuntime().exec("git describe --tags --dirty=+ --match android*")
        val result = process.waitFor()
        if (result != 0) {
            println("Cannot get git describe information. Ignoring...")
            return ""
        }
        return process.inputStream.bufferedReader().readLines().firstOrNull().orEmpty()
    } catch (e: Exception) {
        println("Error getting git description: ${e.message}")
        return ""
    }
}

val sdkSemanticVersionNameBase = "0.0.1"

fun getSdkVersionName(): String {
    // Both sourceBranchName and buildNumber are created as part of the PKGES build job
    return if (!project.hasProperty("buildNumber")) {
        println("************************")
        println("Project does not have property buildNumber. Returning default version= $sdkSemanticVersionNameBase")
        println("************************")
        sdkSemanticVersionNameBase
    } else {
        val sdkVersionNameFull = project.property("buildNumber").toString()
        println("************************")
        println("SDK Version is set to: $sdkVersionNameFull")
        println("************************")
        sdkVersionNameFull
    }
}

val build_versionCode = getCustomVersionCode()
val build_gitDescription = getGitDescription()
val componentizedSDKVersion = getSdkVersionName()
val versionName = componentizedSDKVersion

val build_sdkName = "crossdevicesdk"
val artifactName = "crossdevicesdk"
val mavenGroupId = "com.microsoft.mmx"

// Define the library modules to include in the documentation
val libraryModules = listOf("continuity","crossdeviceextender")

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}

// Add all variables to rootProject.ext for use in Groovy scripts
rootProject.extra.apply {
    set("build_versionCode", build_versionCode)
    set("build_gitDescription", build_gitDescription)
    set("sdkSemanticVersionNameBase", sdkSemanticVersionNameBase)
    set("componentizedSDKVersion", componentizedSDKVersion)
    set("versionName", versionName)
    set("build_sdkName", build_sdkName)
    set("artifactName", artifactName)
    set("mavenGroupId", mavenGroupId)
    set("libraryModules", libraryModules)
}

// Configure detekt for the project
detekt {
    buildUponDefaultConfig = true
    allRules = true
    // Set source sets to only include continuity, crossdeviceextender and partnerapptriggertestapp modules
    source = files(
        "$projectDir/continuity/src/main/java",
        "$projectDir/continuity/src/test/java",
        "$projectDir/crossdeviceextender/src/main/java",
        "$projectDir/crossdeviceextender/src/test/java",
        "$projectDir/partnerapptriggertestapp/src/main/java",
        "$projectDir/partnerapptriggertestapp/src/test/java"
    )
    // Create baseline file if it doesn't exist yet
    baseline = file("${project.rootDir}/config/detekt/baseline.xml")
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    jvmTarget = libs.versions.jvmTarget.get()
    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(false)
        sarif.required.set(true)
    }
}

tasks.register<Copy>("generateKotlinDocs") {
    dependsOn("dokkaHtmlMultiModule", "separateDocZipped")
    from("${buildDir}/dokka/htmlMultiModule")
    into("${rootProject.projectDir}/docs")
    doLast {
        println("Generated and copied Dokka documentation from ${buildDir}/dokka/htmlMultiModule to ${rootProject.projectDir}/docs")
    }
}

tasks.register("separateDocZipped") {
    subprojects {
        val subproject = this
        if (libraryModules.contains(subproject.name)) {
            tasks.register<Zip>("docZipped") {
                dependsOn("dokkaHtml")
                from("${subproject.buildDir}/dokka/html")
                archiveFileName.set("${rootProject.extra["build_sdkName"]}-${subproject.name}-${rootProject.extra["versionName"]}.zip")
                destinationDirectory.set(file("${rootProject.buildDir}/dokka/zip"))
            }
            dependsOn("${subproject.name}:docZipped")
        }
    }
}

// Task to publish continuity module to local staging directory for ESRP Maven Central publishing
tasks.register("publishToMavenCentralStaging") {
    description = "Publishes continuity module to local staging directory for ESRP Maven Central publishing"
    group = "publishing"
    dependsOn("continuity:publishReleasePublicationToMavenCentralStagingRepository")
}

subprojects {
    afterEvaluate {
        if (plugins.hasPlugin("com.android.library")) {
            extensions.configure<com.android.build.gradle.LibraryExtension> {
                publishing {
                    singleVariant("release")
                }

                buildFeatures {
                    buildConfig = true
                }
            }
        }
    }
}
