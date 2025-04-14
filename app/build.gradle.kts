plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    id ("kotlin-kapt")
}

android {
    namespace = "com.example.dalingk"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.dalingk"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation("io.coil-kt.coil3:coil-compose:3.0.4")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.4")

    implementation ("androidx.compose.foundation:foundation:1.5.0")

    implementation ("com.google.accompanist:accompanist-pager:0.28.0")
    implementation ("com.google.accompanist:accompanist-pager-indicators:0.28.0")

    implementation ("com.google.firebase:firebase-appcheck-playintegrity:16.0.0")
    implementation ("com.google.firebase:firebase-appcheck-safetynet:16.0.0")


    implementation ("com.google.firebase:firebase-auth:22.1.1")
    implementation ("com.google.firebase:firebase-firestore:24.9.1")
    implementation("com.google.firebase:firebase-analytics")
    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))

    implementation ("androidx.datastore:datastore-preferences:1.0.0")

    implementation ("androidx.compose.material:material-icons-extended:1.5.0")

    implementation ("androidx.compose.runtime:runtime:1.5.1")

    implementation ("androidx.hilt:hilt-navigation-compose:1.1.0")
    implementation ("com.google.dagger:hilt-android:2.48")

    implementation ("com.google.firebase:firebase-database-ktx:20.3.0")

    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")


    // Cloudinary Android SDK
    implementation ("com.cloudinary:cloudinary-android:3.0.2")

    // Các dependency khác (nếu cần)
    implementation ("androidx.activity:activity-compose:1.9.2")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")

    implementation ("com.karumi:dexter:6.2.3")

    // Download + Preprocess:
    implementation ("com.cloudinary:cloudinary-android-download:3.0.2")
    implementation ("com.cloudinary:cloudinary-android-preprocess:3.0.2")

    implementation ("com.google.android.gms:play-services-base:18.2.0")

    implementation("io.coil-kt:coil-compose:2.4.0") // Đảm bảo phiên bản mới nhất

    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")


    implementation ("androidx.room:room-runtime:2.6.1")
    kapt ("androidx.room:room-compiler:2.6.1")
    implementation ("androidx.room:room-ktx:2.6.1")

    implementation ("androidx.compose.runtime:runtime-livedata:1.6.0")

    implementation ("com.airbnb.android:lottie-compose:6.4.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.play.services.cast.framework)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.androidx.espresso.core)
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.database)
    implementation(libs.androidx.room.common)
    implementation(libs.androidx.room.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)


}