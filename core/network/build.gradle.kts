plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.mac.expensee.core.network"
    compileSdk = 36

    defaultConfig {
        minSdk = 26

        // Configurable base URL: overridden per build type below. A future "point at a different
        // server" debug setting would read/write this through DataStore and rebuild the Retrofit
        // instance (see NetworkModule) rather than requiring a rebuild.
        buildConfigField("String", "API_BASE_URL", "\"https://api.expensee.example.com/\"")
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", "\"https://api.expensee.example.com/\"")
        }
        release {
            buildConfigField("String", "API_BASE_URL", "\"https://api.expensee.example.com/\"")
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    api(project(":core:common"))

    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.koin.android)

    testImplementation(project(":core:testing"))
}
