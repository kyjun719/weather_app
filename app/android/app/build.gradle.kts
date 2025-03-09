plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}
android {
    compileSdk = 34
    namespace = "com.jun.weather"

    defaultConfig {
        applicationId = "com.jun.weather"
        minSdk = 21
        targetSdk = 34
        versionCode = 8
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isDebuggable = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        dataBinding = true
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(libs.bundles.androidx)

    implementation(libs.material)

    testImplementation(libs.junit)
    androidTestImplementation(libs.junit.ext)
    androidTestImplementation(libs.espresso.core)

    implementation(libs.bundles.kotlin)

    implementation(libs.room.runtime)
    kapt(libs.room.complier)

    implementation(libs.coroutine)

    //retrofit2
    implementation(libs.bundles.retrofit2)

    //okhttp3 and logging interceptor
    implementation(libs.bundles.okhttp)

    //joda time
    implementation(libs.jodatime)

    //load csv
    implementation(libs.opencsv)

    //mpchart
    implementation(libs.mpchart)

    //glide
    implementation(libs.glide)
    kapt(libs.glide.compiler)

    //google sdk
    implementation(libs.google.services.location)

    //hilt
    implementation(libs.hilt)
    kapt(libs.hilt.complier)
}
