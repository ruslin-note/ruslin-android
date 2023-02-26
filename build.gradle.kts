// TODO: https://github.com/gradle/gradle/issues/22797 Remove this annotations once we've switched to Gradle 8.1.
@Suppress("DSL_SCOPE_VIOLATION")

buildscript {
    extra.apply {
        set("compose_version", "1.4.0-beta01")
        set("accompanist_version", "0.29.1-alpha")
        set("material3_version", "1.1.0-alpha06")
        set("material_version", "1.4.0-beta01")
        set("lifecycle_runtime_version", "2.6.0-beta01")

        set("compile_sdk_version", 33)
        set("min_sdk_version", 28)
        set("target_sdk_version", 33)
        set("build_tools_version", "30.0.3")
        set("ndk_version", "25.2.9519653")
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