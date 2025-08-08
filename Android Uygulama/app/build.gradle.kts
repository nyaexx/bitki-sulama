plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.bitkisulama"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.bitkisulama"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.5"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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
}

dependencies {
    // Temel Android KTX
    implementation(libs.androidx.core.ktx)


    // Material Components (Toolbar, Button, Dialog vb.)
    implementation("com.google.android.material:material:1.12.0")

    // AppCompat (Eski API desteği için)
    implementation("androidx.appcompat:appcompat:1.6.1")

    // ConstraintLayout (XML’de layout için)
    implementation(libs.androidx.constraintlayout)

    // Splash Screen API (opsiyonel, eğer kullanmıyorsan çıkar)
    implementation("androidx.core:core-splashscreen:1.0.0")

}
