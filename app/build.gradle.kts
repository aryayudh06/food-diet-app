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
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.android.volley:volley:1.2.1")
    implementation ("com.google.android.gms:play-services-location:21.3.0")
    // Add the dependency for the Firebase Authentication library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-auth")
    implementation ("com.google.android.material:material:1.12.0")
    implementation ("androidx.health.connect:connect-client:1.1.0-alpha10")

    // Also add the dependency for the Google Play services library and specify its version
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

    // Dependency for images
    implementation("com.github.bumptech.glide:glide:4.12.0")

    // Dependency for charts
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

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
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.4.0")


    val cameraxVersion = "1.3.4"

    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-video:$cameraxVersion")

    implementation("androidx.camera:camera-view:$cameraxVersion")
    implementation("androidx.camera:camera-extensions:$cameraxVersion")
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
