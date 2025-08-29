import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
    id("org.jetbrains.kotlin.native.cocoapods")
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()


    sourceSets {
        commonMain.dependencies {
            // Ktor Client
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.auth)
            implementation(libs.kotlinx.coroutines.core)

            // Serialización JSON
            implementation(libs.ktor.serialization.kotlinx.json.v238)
            implementation(libs.kotlinx.serialization.json.v160)

            // KMP Secure Storage
            implementation(libs.multiplatform.settings.v130)
            implementation(libs.okio) // Librería para cifrado AES

            // Calendario
            implementation(libs.compose.multiplatform)
            implementation(libs.kotlinx.datetime)

            //
            implementation(libs.kmm.viewmodel.core)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.android)
            implementation(libs.androidx.security.crypto)
            implementation(libs.ktor.client.okhttp) // Ktor para Android

            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.kotlinx.coroutines.play.services)

            implementation(libs.firebase.common.ktx)

            implementation (libs.androidx.lifecycle.viewmodel.ktx)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin) // Ktor para iOS

            implementation(libs.kmm.viewmodel.core)

        }
    }

    cocoapods {
        version = "1.0.0"                    // versión de tu Podspec
        summary = "Módulo compartido para la app de fútbol base"
        homepage = "https://tupagina.com"
        ios.deploymentTarget = "14.0"
        framework {
            baseName = "Shared"
            isStatic = false
        }
    }
}

android {
    namespace = "org.rauldiezmiguel.tfgfutbolbase.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

dependencies {
    implementation(libs.generativeai)
    implementation(libs.androidx.runtime.android)
    implementation(libs.androidx.lifecycle.viewmodel.android)
    implementation(libs.firebase.storage.ktx)
}
