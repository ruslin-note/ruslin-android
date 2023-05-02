@file:Suppress("UnstableApiUsage")

import java.io.FileInputStream
import java.util.Properties
import com.android.build.api.variant.FilterConfiguration.FilterType.*

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("kotlin-android")
    id("kotlin-kapt")
}

fun String.runCommand(workingDir: File = file("./")): String {
    val parts = this.split("\\s".toRegex())
    val process = ProcessBuilder(*parts.toTypedArray())
        .directory(workingDir)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()

    process.waitFor(1, TimeUnit.MINUTES)
    return process.inputStream.bufferedReader().readText().trim()
}

val gitCommitHash = "git rev-parse --verify --short HEAD".runCommand()
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    println("Loading keystore properties from ${keystorePropertiesFile.absolutePath}")
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

val abiCodes = mapOf("x86" to 0, "x86_64" to 1, "armeabi-v7a" to 2, "arm64-v8a" to 3)

android {
    namespace = "org.dianqk.ruslin"
    compileSdk = libs.versions.compileSdkVersion.get().toInt()
    ndkVersion = libs.versions.ndkVersion.get()
    buildToolsVersion = libs.versions.buildToolsVersion.get()

    defaultConfig {
        applicationId = "org.dianqk.ruslin"
        minSdk = libs.versions.minSdkVersion.get().toInt()
        targetSdk = libs.versions.targetSdkVersion.get().toInt()
        versionCode = 6
        versionName = "0.0.1-alpha.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
            generatedDensities("")
        }
    }

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                keyAlias = keystoreProperties["keyAlias"].toString()
                keyPassword = keystoreProperties["keyPassword"].toString()
                storeFile = file(keystoreProperties["storeFile"]!!)
                storePassword = keystoreProperties["storePassword"].toString()
            }
            enableV3Signing = true
        }
    }

    splits {
        abi {
            isEnable = true
            reset()
            if (project.hasProperty("ABI_FILTERS")) {
                for (abi in project.property("ABI_FILTERS").toString().split(";")) {
                    include(abi)
                }
            } else {
                include("x86", "x86_64", "armeabi-v7a", "arm64-v8a")
            }
            isUniversalApk = false
        }
    }

    buildTypes {
        debug {
            buildConfigField("String", "COMMIT_HASH", "\"$gitCommitHash\"")
            applicationIdSuffix = ".debug"
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        release {
            buildConfigField("String", "COMMIT_HASH", "\"$gitCommitHash\"")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")

            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
        freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlin.RequiresOptIn"
        freeCompilerArgs = freeCompilerArgs + "-Xjvm-default=all"
    }

    kotlin {
        jvmToolchain(11)
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        // https://developer.android.com/jetpack/androidx/releases/compose-kotlin
        kotlinCompilerExtensionVersion = "1.4.3"
    }

    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    androidComponents {
        onVariants { variant ->
            variant.outputs.forEach { output ->
                val name = output.filters.find { it.filterType == ABI }?.identifier
                val baseAbiCode = abiCodes[name]!!
                output.versionCode.set(baseAbiCode + (output.versionCode.get() as Int) * 100)
            }
        }
    }


    bundle {
        storeArchive {
            enable = false
        }
    }
}

dependencies {

    implementation(project(":mdrender"))
    implementation(project(":uniffi"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation("androidx.activity:activity-compose:1.7.0")
    implementation("androidx.work:work-runtime-ktx:2.8.1")

    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)

    implementation("androidx.navigation:navigation-compose:2.5.3")
    implementation(libs.compose.material)
    implementation(libs.compose.material.icons.core)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.material3)
    implementation(libs.accompanist.navigation.animation)
    implementation(libs.accompanist.webview)
    implementation(libs.accompanist.systemuicontroller)

    implementation("com.google.android.material:material:1.8.0")

    implementation("com.google.dagger:hilt-android:2.45")
    implementation("androidx.webkit:webkit:1.6.1")
    kapt("com.google.dagger:hilt-compiler:2.45")
    kapt("androidx.hilt:hilt-compiler:1.0.0")
    // https://github.com/google/dagger/issues/2601#issuecomment-1174506373
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")
    implementation("androidx.hilt:hilt-work:1.0.0")

    implementation("androidx.datastore:datastore-preferences:1.0.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}

//tasks.whenTaskAdded {
//    task ->
//        if (task.name == "assembleRelease") {
//            task.doLast {
//                fileTree("$buildDir/intermediates/stripped_native_libs/release/out/lib").visit { details ->
//                    val file = details.getFile()
//                    if (file.isFile() && file.name == "libuniffi_ruslin.so") {
//                        val path = file.path
//                        val sha256 = "sha256sum -b $path".execute().text.trim().split("\\s+")[0]
//                        val length = file.length()
//                        println("sha256sum $path: $sha256 ($length bytes)")
//                    }
//                }
//            }
//        }
//}
