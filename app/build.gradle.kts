plugins {
    alias(libs.plugins.android.application)  // this one works because it's defined via version catalogs
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.techanalysisapp3"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.techanalysisapp3"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.9.7"

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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.github.bumptech.glide:glide:4.14.2")
    implementation ("com.google.mlkit:image-labeling:17.0.7")
    implementation ("com.google.code.gson:gson:2.8.9")
    implementation ("org.jsoup:jsoup:1.15.3")
    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("com.google.android.material:material:1.11.0")
    implementation ("pl.droidsonroids.gif:android-gif-drawable:1.2.28")
    implementation ("com.airbnb.android:lottie:6.4.0")

    annotationProcessor ("com.github.bumptech.glide:compiler:4.14.2")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.12.0"))
    implementation ("com.google.firebase:firebase-auth")
    implementation ("com.google.firebase:firebase-firestore")
    implementation ("com.google.firebase:firebase-storage")

    // OpenStreetMap, Clustering, Location
    implementation ("org.osmdroid:osmdroid-android:6.1.16")
    implementation ("com.github.MKergall:osmbonuspack:6.9.0")

}