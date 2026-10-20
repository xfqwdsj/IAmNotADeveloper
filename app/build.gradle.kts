import com.android.build.api.variant.HostTestBuilder
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.spotless)
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
        version =
            release(37) {
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
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("config")
        }

        debug {
            signingConfig = signingConfigs.getByName("config")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

androidComponents {
    beforeVariants(selector().all()) { variant ->
        variant.hostTests[HostTestBuilder.UNIT_TEST_TYPE]?.enable = true
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
    implementation(libs.compose.material3.adaptive)
    implementation(libs.m3x.material.bundle)
    implementation(libs.m3x.runtime)
    implementation(libs.m3x.settingsPage.core)
    implementation(libs.m3x.settingsPage.material)
    implementation(libs.m3x.settingsPage.datastore)
    ksp(libs.m3x.settingsPage.processor)
    implementation(libs.m3x.resource.compose)
    implementation(libs.m3x.datastore.compose)
    implementation(libs.m3x.datastore.core)
    ksp(libs.m3x.datastore.processor)
    implementation(libs.coil)
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    implementation(libs.datastore)
    implementation(libs.room.ktx)
    implementation(libs.preference)
    ksp(libs.kaidl.compiler)
    implementation(libs.kaidl.runtime)
    compileOnly(libs.libxposed.api)
    implementation(libs.libxposed.service)
}

room {
    schemaDirectory("$projectDir/schemas")
}

spotless {
    kotlin {
        target("src/*/kotlin/**/*.kt")
        ktfmt().kotlinlangStyle()
    }
    kotlinGradle {
        ktfmt().kotlinlangStyle()
    }
}
