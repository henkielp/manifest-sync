plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.manifestscanner"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.manifestscanner"
        minSdk = 24
        targetSdk = 34
        versionCode = 3
        versionName = "0.7.2"
    }

    buildFeatures {
        viewBinding = true
    }

    // NEW: Syncing the Android app to use the Java 17 engine
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // NEW: Syncing Kotlin to use the Java 17 engine
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // 1. AndroidX UI foundations (previously arrived only as transitive
    //    dependencies of Compose and Material; now declared explicitly
    //    because the code imports them directly)
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")

    // 2. Material Components (TabLayout, MaterialButton, cards, FAB,
    //    text fields, and the theme for the XML layouts)
    implementation("com.google.android.material:material:1.11.0")

    // 3. CameraX (To control the phone's hardware camera)
    val cameraxVersion = "1.3.1"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")

    // 4. Google ML Kit (Strictly the Bundled/Offline versions)
    implementation("com.google.mlkit:text-recognition:16.0.1")
    implementation("com.google.mlkit:barcode-scanning:17.3.0")

    // 5. Coroutines (To keep the UI from freezing while the AI thinks)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
}
