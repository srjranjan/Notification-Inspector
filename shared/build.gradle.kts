import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
    id("maven-publish")
    id("signing")
}

group = "io.github.srjranjan"
version = "1.0.0"

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    
    jvm()
    
    js {
        browser()
    }
    
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }
    
    androidLibrary {
       namespace = "com.srj.notificationinspector.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
    }
    
    sourceSets {
        val dbMain by creating {
            dependsOn(commonMain.get())
            kotlin.srcDirs("src/dbMain/kotlin")
            dependencies {
                implementation(libs.androidx.room.runtime)
                implementation(libs.androidx.sqlite.bundled)
            }
        }

        androidMain.get().dependsOn(dbMain)
        iosMain.get().dependsOn(dbMain)
        jvmMain.get().dependsOn(dbMain)

        iosArm64Main.get().dependsOn(iosMain.get())
        iosSimulatorArm64Main.get().dependsOn(iosMain.get())

        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.firebase.messaging)
        }

        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jsMain.dependencies {
            implementation(libs.wrappers.browser)
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspJvm", libs.androidx.room.compiler)
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

afterEvaluate {
    publishing {
        publications {
            withType<MavenPublication> {
                pom {
                    name.set("Notification Inspector")
                    description.set("A high-performance Notification Inspector Kotlin Multiplatform library targeting Android, iOS, JVM, JS, and Wasm.")
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

        repositories {
            maven {
                name = "SonatypeCentral"
                url = uri("https://central.sonatype.com/repository/maven-releases/")
                credentials {
                    username = findPublishingProperty("mavenCentralUsername")
                    password = findPublishingProperty("mavenCentralPassword")
                }
            }
        }
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
            val fallbackFile = file(secretKeyRingPath)
            if (fallbackFile.exists()) fallbackFile.absolutePath else null
        }

        if (resolvedPath != null) {
            project.extra.set("signing.keyId", keyId)
            project.extra.set("signing.password", password)
            project.extra.set("signing.secretKeyRingFile", resolvedPath)
            sign(publishing.publications)
        } else {
            println("GPG key ring file not found at: ${secretKeyRingFile.absolutePath}")
            isRequired = false
        }
    } else {
        println("Signing credentials not found, signing is skipped.")
    }
}