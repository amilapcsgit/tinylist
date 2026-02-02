plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("org.jetbrains.kotlin.kapt")
  id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
  namespace = "com.cyberlist.neonlist"
  compileSdk = 35

  defaultConfig {
    applicationId = "com.cyberlist.neonlist"
    minSdk = 31
    targetSdk = 35
    versionCode = 9
    versionName = "0.81"
  }

  val keystorePath = (project.findProperty("KEYSTORE_PATH") as String?) ?: System.getenv("KEYSTORE_PATH")
  val keystorePassword = (project.findProperty("KEYSTORE_PASSWORD") as String?) ?: System.getenv("KEYSTORE_PASSWORD")
  val keyAlias = (project.findProperty("KEY_ALIAS") as String?) ?: System.getenv("KEY_ALIAS")
  val keyPassword = (project.findProperty("KEY_PASSWORD") as String?) ?: System.getenv("KEY_PASSWORD")

  signingConfigs {
    getByName("debug")
    create("release") {
      if (!keystorePath.isNullOrBlank()) {
        storeFile = file(keystorePath)
      }
      storePassword = keystorePassword
      this.keyAlias = keyAlias
      this.keyPassword = keyPassword
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      signingConfig = if (keystorePath.isNullOrBlank()) {
        signingConfigs.getByName("debug")
      } else {
        signingConfigs.getByName("release")
      }
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
    }
  }

  buildFeatures {
    compose = true
  }

    compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  kotlinOptions {
    jvmTarget = "17"
    freeCompilerArgs += "-Xcontext-receivers"
  }
    dependenciesInfo {
        includeInApk = true
    }
}

dependencies {
  val composeBom = platform("androidx.compose:compose-bom:2025.08.00")
  implementation(composeBom)
  androidTestImplementation(composeBom)

  implementation("androidx.activity:activity-compose:1.9.2")
  implementation("androidx.compose.animation:animation-android")
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.ui:ui-tooling-preview")
  implementation("androidx.compose.foundation:foundation")
  implementation("androidx.compose.material3:material3:1.2.1")
  implementation("androidx.compose.material:material-icons-extended:1.6.8")
  implementation("com.google.android.material:material:1.12.0")
  implementation("androidx.navigation:navigation-compose:2.7.7")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
  implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
  implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

  implementation("androidx.room:room-runtime:2.8.4")
  kapt("androidx.room:room-compiler:2.8.4")
  implementation("androidx.room:room-ktx:2.8.4")

  implementation("androidx.datastore:datastore-preferences:1.1.1")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

  implementation("sh.calvin.reorderable:reorderable:3.0.0")

  debugImplementation("androidx.compose.ui:ui-tooling")
  debugImplementation("androidx.compose.ui:ui-test-manifest")
}
