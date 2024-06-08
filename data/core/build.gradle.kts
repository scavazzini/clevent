plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin)
    id("kotlin-kapt")
}

android {
    namespace = "dev.scavazzini.clevent.data.core"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        consumerProguardFiles(
            "retrofit2-consumer-rules.pro",
            "room-consumer-rules.pro",
            "protobuf-consumer-rules.pro",
        )
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.version.get().toString()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        release {
            isMinifyEnabled = true
        }
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

dependencies {
    // Preferences
    implementation(libs.androidx.preference.ktx)

    // WorkManager
    implementation(libs.workManager)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.common)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.hilt.workManager)
    kapt(libs.hilt.compiler)

    // Protobuf
    implementation(libs.protobuf.javalite)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)

    // Retrofit
    implementation(libs.bundles.retrofit)

    // Tests
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.inline)
    androidTestImplementation(libs.mockito.android)
    androidTestImplementation(libs.androidx.junit)
}
