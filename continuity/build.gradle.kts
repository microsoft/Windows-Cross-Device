plugins {
    alias(libs.plugins.android.library)
    id("org.jlleitschuh.gradle.ktlint")
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.dokka)
}

apply(from = rootDir.resolve("gradle/maven-publish.gradle"))

android {
    namespace = "com.microsoft.crossdevicesdk.continuity"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()

        buildConfigField("String", "VERSION_NAME", "\"${rootProject.extra["versionName"]}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            consumerProguardFiles("consumer-rules.pro")
        }
        debug {
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

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.annotation)
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.kotlin.stdlib.jdk7)
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
}

tasks.dokkaHtmlPartial {
    outputDirectory.set(file("build/docs/partial"))
    dokkaSourceSets.configureEach {
        moduleName.set("Cross Device Continuity SDK Documentation")
        includes.from("src/main/java/com/microsoft/crossdevicesdk/continuity/package-info.md")
    }
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    android.set(true)
    outputColorName.set("RED")
}
