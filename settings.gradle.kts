rootProject.name = "NotificationInspector"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

fun findPublishingProperty(name: String): String? {
    val localFile = file("local.properties")
    if (localFile.exists()) {
        val properties = java.util.Properties().apply {
            localFile.inputStream().use { load(it) }
        }
        val value = properties.getProperty(name)
        if (!value.isNullOrBlank()) return value
    }
    val gradleProperty = providers.gradleProperty(name).orNull
    if (!gradleProperty.isNullOrBlank()) return gradleProperty
    val envName = name.replace(".", "_").replace(Regex("([a-z])([A-Z])"), "$1_$2").uppercase()
    val envValue = System.getenv(envName)
    if (!envValue.isNullOrBlank()) return envValue
    return null
}

val target = findPublishingProperty("publishTarget") ?: "android"
val isAndroidOnly = !target.equals("all", ignoreCase = true)

include(":shared")
include(":shared-no-op")
include(":androidApp")
if (!isAndroidOnly) {
    include(":desktopApp")
    include(":webApp")
}