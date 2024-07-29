plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin)
    id("kotlin-kapt")
}

android {
    namespace = "dev.scavazzini.clevent.feature.settings.domain"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:domain"))

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
}
