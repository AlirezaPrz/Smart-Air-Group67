plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.smartairsetup"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.smartairsetup"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // --- AndroidX & UI stuff ---
    implementation(libs.core.ktx)
    implementation(libs.appcompat.v170)
    implementation(libs.material.v1120)
    implementation(libs.constraintlayout.v221)

    // (If your project had other libs from the template, add them back too.)

    // --- Firebase ---
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
}