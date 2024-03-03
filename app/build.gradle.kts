plugins {
    alias(libs.plugins.agp)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.hilt)
    alias(libs.plugins.protobuf)
    id("kotlin-kapt")
}

android {
    namespace = "dev.scavazzini.clevent"
    compileSdk = 34

    defaultConfig {
        applicationId = "dev.scavazzini.clevent"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    protobuf {
        protoc {
            artifact = libs.protobuf.protoc.get().toString()
        }

        generateProtoTasks {
            all().forEach { task ->
                task.builtins {
                    register("java") {
                        option("lite")
                    }
                }
            }
        }
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
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
                "retrofit2.pro",
                "room.pro",
            )
        }
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)

    // Preferences
    implementation(libs.androidx.preference.ktx)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    kapt(libs.hilt.compiler)

    // Zxing (QR Code)
    implementation(libs.zxing.core)

    // Protobuf
    implementation(libs.protobuf.javalite)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)

    // Tests (JUnit, Mockito and Espresso)
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.inline)
    androidTestImplementation(libs.mockito.android)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    // Retrofit
    implementation(libs.bundles.retrofit)
}
