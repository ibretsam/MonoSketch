/*
 * Copyright (c) 2023, tuanchauict
 */

plugins {
    kotlin("multiplatform")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(projects.buildEnvironment)
    implementation(projects.commons)
    implementation(projects.livedata)

    implementation(libs.kotlin.stdlib.js)

    testImplementation(libs.kotlin.test.js)
}

val compilerType: org.jetbrains.kotlin.gradle.plugin.KotlinJsCompilerType by ext
kotlin {
    js(compilerType) {
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
    }
}
