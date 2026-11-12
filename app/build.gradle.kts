plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.courtquestpro"
    compileSdk = 35
    buildToolsVersion = "35.0.1"

    defaultConfig {
        applicationId = "com.courtquestpro"
        minSdk = 26
        targetSdk = 35
        versionCode = 11
        versionName = "1.0"
    }

    buildFeatures { compose = true }
    signingConfigs {
        getByName("debug") {
            storeFile = rootProject.file("signing/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.webkit:webkit:1.12.1")
    implementation("com.google.code.gson:gson:2.11.0")

    // Owner-approved premium flow toolkit. Keep these compatible with Kotlin 2.0.21
    // and Compose BOM 2024.12.01; see docs/tooling/PREMIUM_FLOW_TOOLKIT.md.
    implementation("com.airbnb.android:lottie-compose:6.7.1")
    implementation("com.canopas.intro-showcase-view:introshowcaseview:2.0.2")
    implementation("dev.chrisbanes.haze:haze:1.1.1")
    testImplementation("junit:junit:4.13.2")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
