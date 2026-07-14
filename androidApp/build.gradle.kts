import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties
import kotlin.apply

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(projects.shared)

    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}
val baseVersionCode = findPublishingProperty("versionCode")?.toInt() ?: 13
val baseVersionName = findPublishingProperty("libVersion") ?: "1.0.13"

android {
    namespace = "com.srj.notificationinspector"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.srj.notificationinspector"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = baseVersionCode
        versionName = baseVersionName
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
fun findPublishingProperty(name: String): String? {
    // 1. Try local.properties
    val localProperties = Properties().apply {
        val file = rootProject.file("local.properties")
        if (file.exists()) {
            file.inputStream().use { load(it) }
        }
    }
    val localValue = localProperties.getProperty(name)
    if (!localValue.isNullOrBlank()) return localValue

    // 2. Try gradle property (gradle.properties or system properties)
    val gradleValue = providers.gradleProperty(name).orNull
    if (!gradleValue.isNullOrBlank()) return gradleValue

    // 3. Try environment variable (mapping names e.g., mavenCentralUsername -> MAVEN_CENTRAL_USERNAME)
    val envName = name.replace(".", "_").replace(Regex("([a-z])([A-Z])"), "$1_$2").uppercase()
    val envValue = providers.environmentVariable(envName).orNull
    if (!envValue.isNullOrBlank()) return envValue

    return null
}