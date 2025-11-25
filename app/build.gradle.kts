plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.speedometer.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.speedometer.app"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        ndk {
            abiFilters += listOf("arm64-v8a")
        }

        androidResources {
            localeFilters += setOf("en")
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file("sm.p12")
            storePassword = "1234"
            keyAlias = "US"
            keyPassword = "1234"
            storeType = "pkcs12"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation(platform("androidx.compose:compose-bom:2025.11.00"))
    implementation("androidx.compose.ui:ui:1.10.0-beta01")
    implementation("androidx.compose.animation:animation:1.9.0-rc01")
    implementation("androidx.compose.ui:ui-tooling:1.10.0-alpha01")
    implementation("androidx.compose.ui:ui-tooling-preview:1.10.0-beta01")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-compose:1.12.0-alpha06")
    implementation("androidx.compose.material:material-icons-core:1.7.8")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("androidx.compose.runtime:runtime:1.10.0-alpha01")
    implementation("androidx.compose.material3:material3:1.5.0-alpha02")
    implementation("androidx.compose.foundation:foundation:1.10.0-beta01")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
}