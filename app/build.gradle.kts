plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.azaldev.garden"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.azaldev.garden"
        minSdk = 29
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")


    /**
     * Room implementation
     */
    val room_version = "2.5.0"
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    kapt("androidx.room:room-compiler:$room_version")            // To use Kotlin annotation processing tool (kapt)
    implementation("androidx.room:room-ktx:$room_version")       // optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-rxjava2:$room_version") // optional - RxJava2 support for Room
    implementation("androidx.room:room-rxjava3:$room_version") // optional - RxJava3 support for Room
    implementation("androidx.room:room-guava:$room_version")   // optional - Guava support for Room, including Optional and ListenableFuture
//    testImplementation("androidx.room:room-testing:$room_version")// optional - Test helpers
    implementation("androidx.room:room-paging:$room_version")     // optional - Paging 3 Integration

    /**
     * Material UI 3 integration
     */
    val material3_version = "1.1.2"
    implementation("androidx.compose.material3:material3:$material3_version")
    implementation("androidx.compose.material3:material3-window-size-class:$material3_version")


    /**
     * Socket.io integration
     */
    implementation("io.socket:socket.io-client:2.1.0")

    implementation("com.journeyapps:zxing-android-embedded:4.2.0")
    implementation("com.google.zxing:core:3.4.0")

    /**
     * Debug utility resources
     * use debugImplementation
     */
//    implementation("com.facebook.stetho:stetho:1.6.0")
//    implementation("com.facebook.stetho:stetho-okhttp3:1.6.0")
//    implementation("com.amitshekhar.android:debug-db:1.0.6")


    testImplementation("junit:junit:4.13.2")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}