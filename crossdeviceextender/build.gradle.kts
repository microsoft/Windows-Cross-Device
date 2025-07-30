plugins {
    id("com.android.library")
    id("maven-publish")
    id("org.jlleitschuh.gradle.ktlint")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.dokka")
    id("kotlin-parcelize")
}

apply(from = "${rootDir}/gradle/maven-publish.gradle")

dependencies {
    implementation "androidx.core:core-ktx:${ktxCoreVersion}"
    testImplementation "androidx.test.ext:junit-ktx:${androidxJunitKtxVersion}"
    testImplementation "junit:junit:${junitVersion}"
    testImplementation "org.mockito:mockito-core:${mockitoCoreVersion}"
    testImplementation "org.robolectric:robolectric:${robolectricVersion}"
    androidTestImplementation "androidx.test.ext:junit:${androidxTestJunitVersion}"
    androidTestImplementation "androidx.test.espresso:espresso-core:${androidxTestEspressoCore}"
}

android {
    namespace = "com.microsoft.crossdevicesdk.crossdeviceextender"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro')
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

dokkaHtmlPartial {
    outputDirectory.set(file("build/docs/partial"))
    dokkaSourceSets {
        configureEach {
            moduleName = "Cross Device Extender SDK Documentation"
            includes.from("src/main/java/com/microsoft/crossdevicesdk/crossdeviceextender/package-info.md")
        }
    }
}

dokkaHtml {
    dokkaSourceSets {
        configureEach {
            moduleName = "Cross Device Extender SDK Documentation"
            includes.from("src/main/java/com/microsoft/crossdevicesdk/crossdeviceextender/package-info.md")
        }
    }
}

ktlint {
    android = true
    outputColorName = "RED"
}