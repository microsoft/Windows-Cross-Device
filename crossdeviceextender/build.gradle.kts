plugins {
    id("com.android.library")
    id("maven-publish")
    id("org.jlleitschuh.gradle.ktlint")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.dokka")
    id("kotlin-parcelize")
}

apply(from = rootDir.resolve("gradle/maven-publish.gradle"))

dependencies {
    implementation(libs.core.ktx)

    testImplementation(libs.androidx.junit.ktx)
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.robolectric)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
}

android {
    namespace = "com.microsoft.crossdevicesdk.crossdeviceextender"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        buildConfigField("String", "VERSION_NAME", "\"${rootProject.extra["versionName"]}\"")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.valueOf(
            "VERSION_${libs.versions.jvmTarget.get().replace(".", "_")}"
        )
        targetCompatibility = JavaVersion.valueOf(
            "VERSION_${libs.versions.jvmTarget.get().replace(".", "_")}"
        )
    }
    libraryVariants.all {
        outputs.all {
            val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            val sdkName = rootProject.extra["build_sdkName"] as String
            val version = rootProject.extra["versionName"] as String
            output.outputFileName = "$sdkName-${project.name}-$version-$name.aar"
        }
    }
    kotlinOptions {
        jvmTarget = libs.versions.jvmTarget.get()
    }
}
tasks.dokkaHtmlPartial {
    outputDirectory.set(file("build/docs/partial"))
    dokkaSourceSets.configureEach {
        moduleName.set("Cross Device Extender SDK Documentation")
        includes.from(
            "src/main/java/com/microsoft/crossdevicesdk/crossdeviceextender/package-info.md"
        )
    }
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    android.set(true)
    outputColorName.set("RED")
}
