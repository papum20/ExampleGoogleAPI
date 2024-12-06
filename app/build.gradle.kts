plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    // for Firebase
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.examplesgoogleapi"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.examplesgoogleapi"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"

            // fix `More than one file was found with OS independent path 'META-INF/LICENSE'`,
            // with oauth2 and credentials
            // https://stackoverflow.com/a/47509465/20607105
            resources.excludes.add("META-INF/*")
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)


    /* Google authentication/authorization */

    implementation("androidx.credentials:credentials:1.5.0-beta01")
    // optional - needed for credentials support from play services, for devices running
    // Android 13 and below.
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0-beta01")

    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")


    /* Google Drive API */
    implementation("com.google.android.gms:play-services-auth:21.2.0")	// for google sign in

    // for drive integration
    implementation("com.google.http-client:google-http-client-gson:1.45.0")

    implementation("com.google.api-client:google-api-client-android:2.7.0") {
        exclude(group = "org.apache.httpcomponents")
        exclude(module = "guava-jdk5")
    }
    implementation("com.google.apis:google-api-services-drive:v3-rev136-1.25.0") {
        exclude(group = "org.apache.httpcomponents")
        exclude(module = "guava-jdk5")
    }

    // OAuth
    implementation("com.google.auth:google-auth-library-oauth2-http:1.23.0")

    // Guava
    implementation("com.google.guava:guava:33.1.0-jre")
    // Guava fix
    implementation("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")

    /* Firebase */

    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))

    // Add the dependency for the Firebase Authentication library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-auth")



}