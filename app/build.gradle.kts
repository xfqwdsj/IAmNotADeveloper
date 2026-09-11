import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    id("kotlin-parcelize")
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
        freeCompilerArgs.add("-Xexplicit-context-arguments")
    }
}

android {
    val appId = "top.ltfan.notdeveloper"

    namespace = appId
    compileSdk {
        version = release(37) {
            minorApiLevel = 2
        }
    }

    signingConfigs {
        create("config") {
            storeFile = file("key.jks")
            storePassword = "keykey"
            keyAlias = "keykey"
            keyPassword = "keykey"
        }
    }

    defaultConfig {
        applicationId = appId
        minSdk = 27
        targetSdk = 37
        versionName = "1.7.0"
        versionCode = 13
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("config")
        }

        debug {
            signingConfig = signingConfigs.getByName("config")
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.serialization.cbor)
    implementation(libs.lifecycle.runtime)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.viewmodelNav3)
    implementation(libs.splashscreen)
    implementation(libs.activity)
    implementation(libs.navigation.runtime)
    implementation(libs.navigation.ui)
    implementation(platform(libs.compose))
    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.animation)
    implementation(libs.compose.material3)
//    implementation(libs.compose.material3.adaptive)
    implementation(libs.compose.icons)
    implementation(libs.capsule)
    implementation(libs.backdrop)
    implementation(libs.m3Extended)
    implementation(libs.coil)
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    implementation(libs.datastore)
    implementation(libs.room.ktx)
    implementation(libs.preference)
    implementation(libs.dslUtilities)
    ksp(libs.kaidl.compiler)
    implementation(libs.kaidl.runtime)
    compileOnly(libs.libxposed.api)
    implementation(libs.libxposed.service)
}

room {
    schemaDirectory("$projectDir/schemas")
}
