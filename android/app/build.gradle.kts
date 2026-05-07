import java.util.Properties

plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("org.jetbrains.kotlin.kapt")
  id("org.jetbrains.kotlin.plugin.serialization")
  id("org.jetbrains.kotlin.plugin.compose")
}

val appId = providers.gradleProperty("APP_ID").get()
val appDisplayName = providers.gradleProperty("APP_DISPLAY_NAME").get()
val appVersionCode = providers.gradleProperty("APP_VERSION_CODE").get().toInt()
val appVersionName = providers.gradleProperty("APP_VERSION_NAME").get()
val releasePropertiesFile = rootProject.file("release.properties")
val releaseProperties = Properties().apply {
  if (releasePropertiesFile.isFile) {
    releasePropertiesFile.inputStream().use(::load)
  }
}

fun releaseValue(name: String): String? =
  (project.findProperty(name) as String?)
    ?: System.getenv(name)
    ?: releaseProperties.getProperty(name)

android {
  namespace = appId
  compileSdk = 35

  defaultConfig {
    applicationId = appId
    minSdk = 29
    targetSdk = 35
    versionCode = appVersionCode
    versionName = appVersionName

    manifestPlaceholders["appLabel"] = appDisplayName
    resValue("string", "app_name", appDisplayName)
    buildConfigField("String", "APP_DISPLAY_NAME", "\"$appDisplayName\"")
  }

  val keystorePath = releaseValue("KEYSTORE_PATH")
  val keystorePassword = releaseValue("KEYSTORE_PASSWORD")
  val keyAlias = releaseValue("KEY_ALIAS")
  val keyPassword = releaseValue("KEY_PASSWORD")
  val isReleaseSigningConfigured =
    !keystorePath.isNullOrBlank() &&
      !keystorePassword.isNullOrBlank() &&
      !keyAlias.isNullOrBlank() &&
      !keyPassword.isNullOrBlank()
  val isReleaseTaskRequested = gradle.startParameter.taskNames.any { it.contains("Release", ignoreCase = true) }

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
      if (isReleaseTaskRequested && !isReleaseSigningConfigured) {
        throw GradleException(
          "Release build requires KEYSTORE_PATH, KEYSTORE_PASSWORD, KEY_ALIAS, and KEY_PASSWORD. Refusing to sign with debug."
        )
      }
      isMinifyEnabled = false
      isShrinkResources = false
      signingConfig = signingConfigs.getByName("release")
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
    }
  }

  buildFeatures {
    compose = true
    buildConfig = true
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

kapt {
  arguments {
    arg("room.schemaLocation", "$projectDir/schemas")
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
  implementation("androidx.compose.material3:material3")
  implementation("androidx.compose.material:material-icons-extended")
  implementation("com.google.android.material:material:1.12.0")
  implementation("androidx.navigation:navigation-compose:2.7.7")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
  implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
  implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

  implementation("androidx.room:room-runtime:2.8.4")
  kapt("androidx.room:room-compiler:2.8.4")
  implementation("androidx.room:room-ktx:2.8.4")

  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
  implementation("com.jakewharton.timber:timber:5.0.1")

  implementation("sh.calvin.reorderable:reorderable:3.0.0")

  debugImplementation("androidx.compose.ui:ui-tooling")
  debugImplementation("androidx.compose.ui:ui-test-manifest")

  testImplementation("junit:junit:4.13.2")
}
