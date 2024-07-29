plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

kotlin {
    jvmToolchain(21)
}

android {
    namespace = "com.estzhe.timer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.estzhe.timer"
        versionCode = 1
        versionName = "1.0"

        minSdk = 26
        targetSdk = 34
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
        }
    }
}

dependencies {
    implementation("androidx.wear:wear:1.3.0")
    implementation("com.google.android.support:wearable:2.9.0")
    compileOnly("com.google.android.wearable:wearable:2.9.0")

    implementation("androidx.preference:preference-ktx:1.2.1")

    implementation("com.google.code.gson:gson:2.11.0")
}
