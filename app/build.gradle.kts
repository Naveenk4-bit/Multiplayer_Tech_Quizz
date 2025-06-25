plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.codeiq"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.codeiq"
        minSdk = 24
        targetSdk = 35
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
}

dependencies {
    // ✅ Optimized POI for Android (Removes Log4j Dependency)
    implementation("org.apache.poi:poi-ooxml-lite:5.2.3")

    // ✅ Required for Excel Parsing (Minimal Dependencies)
    implementation("org.apache.commons:commons-compress:1.21")
    implementation("org.apache.xmlbeans:xmlbeans:5.1.1")
    implementation ("com.opencsv:opencsv:5.7.1")


    // 🔥 Firebase and UI Dependencies
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.database)

    // 🛠️ Test Dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

// ✅ Force exclude Log4j to prevent MethodHandle.invoke error
configurations.all {
    exclude(group = "org.apache.logging.log4j", module = "log4j-core")
    exclude(group = "org.apache.logging.log4j", module = "log4j-api")
}
