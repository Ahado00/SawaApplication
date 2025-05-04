plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
    // Hilt DI
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    // Firebase
    alias(libs.plugins.google.firebase.crashlytics)
    // Serialization Dep
    kotlin("plugin.serialization") version "2.0.0"
}

android {
    namespace = "com.example.sawaapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.sawaapplication"
        minSdk = 24
        targetSdk = 35
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Core and Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Compose
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    //Firebase dependencies
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.messaging.ktx)

    // Icons and Material
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.compose.material3.material3)

    // Hilt dependencies
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    kapt(libs.hilt.android.compiler)

    // Navigation dependencies
    implementation(libs.androidx.navigation.compose)

    // For encrypted SharedPreferences
    implementation(libs.androidx.security.crypto)

    // Retrofit core library for making HTTP requests (GET, POST, etc.)
    implementation(libs.retrofit)

    // OkHttp logging interceptor for debugging network requests/responses in Logcat
    implementation(libs.logging.interceptor)

    // Google Maps SDK for Android
    implementation(libs.places)
    implementation(libs.play.services.maps)

    // Google maps Compose
    implementation(libs.maps.compose)

    // Serialization Dependency
    implementation(libs.kotlinx.serialization.json)

}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}