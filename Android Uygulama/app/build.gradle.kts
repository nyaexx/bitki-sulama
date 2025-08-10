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
        versionName = "1.6"
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

    implementation(libs.androidx.core.ktx)



    implementation(libs.material)


    implementation("androidx.appcompat:appcompat:1.6.1")


    implementation(libs.androidx.constraintlayout)

 

}
