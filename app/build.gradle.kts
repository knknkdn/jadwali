plugins { id("com.android.application") }
android {
    namespace = "com.jadwali.app"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.jadwali.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("com.google.mlkit:text-recognition:16.0.1")
}
