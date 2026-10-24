plugins { id("com.android.application") }

android {
    namespace = "app.purebit"
    compileSdk = 37
    ndkVersion = "28.2.13676358"
    defaultConfig {
        applicationId = "app.purebit"
        minSdk = 31
        targetSdk = 37
        versionCode = 47
        versionName = "1.10.0"
        ndk { abiFilters += listOf("arm64-v8a", "x86_64") }
        externalNativeBuild { cmake { arguments += "-DANDROID_STL=c++_static" } }
        testInstrumentationRunner = "android.test.InstrumentationTestRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures { buildConfig = true }
    buildTypes { release { isMinifyEnabled = false } }
    externalNativeBuild { cmake { path = file("src/main/cpp/CMakeLists.txt"); version = "3.22.1" } }
    packaging { resources.excludes += setOf("META-INF/services/javax.sound.sampled.spi.*") }
}

dependencies {
    implementation("org.jflac:jflac-codec:1.5.2")
    testImplementation("junit:junit:4.13.2")
}
