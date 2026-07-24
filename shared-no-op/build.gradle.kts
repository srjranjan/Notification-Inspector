import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    id("maven-publish")
    id("signing")
    id("com.gradleup.nmcp") version "1.5.0"
}

group = "io.github.srjranjan"

val baseVersion = findPublishingProperty("libVersion") ?: "1.0.0"
val suffix = findPublishingProperty("stagingSuffix") ?: ""
version = if (suffix.isNotEmpty()) "$baseVersion-$suffix" else baseVersion

kotlin {
    val target = findPublishingProperty("publishTarget") ?: "android"
    val isAndroidOnly = !target.equals("all", ignoreCase = true)

    if (!isAndroidOnly) {
        val xcf = XCFramework("NotificationInspectorNoOp")
        listOf(
            iosArm64(),
            iosSimulatorArm64()
        ).forEach { iosTarget ->
            iosTarget.binaries.framework {
                baseName = "NotificationInspectorNoOp"
                xcf.add(this)
                isStatic = true
            }
        }
    }

    androidLibrary {
       namespace = "com.srj.notificationinspector.shared.noop"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }

        if (!isAndroidOnly) {
            val iosMain by getting
            val iosArm64Main by getting
            val iosSimulatorArm64Main by getting

            iosArm64Main.dependsOn(iosMain)
            iosSimulatorArm64Main.dependsOn(iosMain)
        }
    }
}

fun findPublishingProperty(name: String): String? {
    val localFile = rootProject.file("local.properties")
    if (localFile.exists()) {
        val properties = Properties().apply {
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

afterEvaluate {
    publishing {
        publications {
            withType<MavenPublication> {
                pom {
                    name.set("Notification Inspector No-Op")
                    description.set("A no-op configuration variant of the Notification Inspector library for production Android builds.")
                    url.set("https://github.com/srjranjan/Notification-Inspector")

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("srjranjan")
                            name.set("SRJ")
                            email.set("sudhanshuwithyoualll@gmail.com")
                        }
                    }
                    scm {
                        connection.set("scm:git:github.com/srjranjan/Notification-Inspector.git")
                        developerConnection.set("scm:git:ssh://github.com/srjranjan/Notification-Inspector.git")
                        url.set("https://github.com/srjranjan/Notification-Inspector")
                    }
                }
            }
        }
    }
}

nmcp {
    publishAllPublicationsToCentralPortal {
        username.set(findPublishingProperty("mavenCentralUsername"))
        password.set(findPublishingProperty("mavenCentralPassword"))
    }
}

signing {
    val keyId = findPublishingProperty("signing.keyId")
    val password = findPublishingProperty("signing.password")
    val secretKeyRingPath = findPublishingProperty("signing.secretKeyRingFile")

    val isSigningConfigured = !keyId.isNullOrBlank() && !password.isNullOrBlank() && !secretKeyRingPath.isNullOrBlank()
    isRequired = isSigningConfigured

    if (isSigningConfigured) {
        val secretKeyRingFile = rootProject.file(secretKeyRingPath!!)
        val resolvedPath = if (secretKeyRingFile.exists()) {
            secretKeyRingFile.absolutePath
        } else {
            val fallbackFile = rootProject.file(secretKeyRingPath)
            if (fallbackFile.exists()) fallbackFile.absolutePath else null
        }

        if (resolvedPath != null) {
            project.extra.set("signing.keyId", keyId)
            project.extra.set("signing.password", password)
            project.extra.set("signing.secretKeyRingFile", resolvedPath)
            sign(publishing.publications)
        }
    }
}
