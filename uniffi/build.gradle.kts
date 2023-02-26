@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "uniffi.ruslin"
    compileSdk = libs.versions.compileSdkVersion.get().toInt()
    ndkVersion = libs.versions.ndkVersion.get()
    buildToolsVersion = libs.versions.buildToolsVersion.get()

    defaultConfig {
        minSdk = libs.versions.minSdkVersion.get().toInt()
//        targetSdk = libs.versions.targetSdkVersion.get().toInt()
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    api(libs.androidx.core.ktx)
    implementation("net.java.dev.jna:jna:5.13.0@aar")
}