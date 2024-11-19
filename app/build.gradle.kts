import java.util.Properties

//apply(from = "readEnv.gradle.kts")

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.pam.gemastik_app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.pam.gemastik_app"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "GEMINI_KEY", "\"${getEnvVar("GEMINI_API_KEY")}\"")
        buildConfigField("String", "WEATHERBIT_KEY", "\"${getEnvVar("WEATHERBIT_API_KEY")}\"")
        buildConfigField("String", "FB_DB_KEY", "\"${getEnvVar("FIREBASE_DATABASE")}\"")
        buildConfigField("String", "CLIENT_ID", "\"${getEnvVar("CLIENT_ID")}\"")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
        mlModelBinding = true
    }
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.generativeai)
    // Import the BoM for the Firebase platform
    implementation(platform(libs.firebase.bom.v3312))
    implementation(libs.volley)
    implementation (libs.play.services.location)
    // Add the dependency for the Firebase Authentication library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation(libs.google.firebase.auth)
    implementation (libs.material)
    implementation (libs.androidx.connect.client)
    implementation (libs.material.v170)

    // Also add the dependency for the Google Play services library and specify its version
    implementation(libs.play.services.auth)
    implementation(libs.generativeai)

    // Dependency for images
    implementation(libs.glide)

    // Dependency for charts
    implementation(libs.mpandroidchart)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.firebase.database)
    implementation(libs.androidx.gridlayout)
    implementation(libs.androidx.runtime.android)
    implementation(libs.tensorflow.lite.support)
    implementation(libs.tensorflow.lite.metadata)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation (libs.kotlinx.coroutines.play.services)


    implementation (libs.androidx.lifecycle.runtime.ktx)




    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.video)

    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.extensions)
}

fun getEnvVar(name: String): String? {
    val envFile = rootProject.file(".env")
    if (envFile.exists()) {
        val envVars = Properties()
        envFile.bufferedReader().use { reader ->
            envVars.load(reader)
        }
        return envVars.getProperty(name)
    }
    return null
}
