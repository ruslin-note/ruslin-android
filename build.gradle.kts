buildscript {
    repositories {
        mavenCentral()
        google()
    }
}

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.kotlin.gradlePlugin) apply false
    alias(libs.plugins.androidx.benchmark) apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}