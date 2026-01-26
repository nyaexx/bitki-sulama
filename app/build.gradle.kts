plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "gndsalih.nyaexx.bitkisulama"
    compileSdk = 36

    defaultConfig {
        applicationId = "gndsalih.nyaexx.bitkisulama"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "n1.7.1"
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
