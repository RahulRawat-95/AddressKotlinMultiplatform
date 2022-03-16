import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import java.net.URI

val kotlin_version: String by extra

buildscript {
    var kotlin_version: String by extra
    kotlin_version = "1.6.10"
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", kotlin_version))
    }
}

plugins {
    kotlin("multiplatform") version "1.6.10"
    id("maven-publish")
    id("com.android.library")
    kotlin("plugin.serialization") version "1.6.10"
    id("com.squareup.sqldelight") version "1.5.3"
}

group = "me.rahulrawat"
version = "1.0-SNAPSHOT"

val xcFrameworkName = "AddressLib"

repositories {
    google()
    mavenCentral()
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
        testRuns["test"].executionTask.configure {
            useJUnit()
        }
    }
    js(IR) {
        useCommonJs()
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
            }
        }
    }

    val xcFramework = XCFramework(xcFrameworkName)

    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    when {
        hostOs == "Mac OS X" ->
            macosX64("native") {
                binaries.framework(xcFrameworkName) {
                    xcFramework.add(this)
                }
            }
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    android {
        publishLibraryVariants("release", "debug")
    }

    ios {
        binaries.framework(xcFrameworkName) {
            xcFramework.add(this)
        }
    }

    val coroutinesVersion = "1.6.0-native-mt"
    val serializationVersion = "1.3.1"
    val ktorVersion = "1.6.7"
    val sqlDelightVersion = "1.5.3"
    val kodeinVersion = "7.10.0"

    sourceSets {
        val commonMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion"){
                    version {
                        strictly(coroutinesVersion)
                    }
                }
                api("org.jetbrains.kotlinx:kotlinx-serialization-core:$serializationVersion")

                api("io.ktor:ktor-client-core:$ktorVersion")
                api("io.ktor:ktor-client-serialization:$ktorVersion")
                api("io.ktor:ktor-client-logging:$ktorVersion")

                api("com.squareup.sqldelight:runtime:$sqlDelightVersion")
                api("com.squareup.sqldelight:coroutines-extensions:$sqlDelightVersion")

                api("org.kodein.di:kodein-di:$kodeinVersion")
            }
        }
        val commonTest by getting
        val jvmMain by getting {
            dependencies {
                api("io.ktor:ktor-client-java:$ktorVersion")
                api("com.squareup.sqldelight:sqlite-driver:$sqlDelightVersion")
            }
        }
        val jvmTest by getting
        val jsMain by getting {
            dependencies {
                api("io.ktor:ktor-client-js:$ktorVersion")
                api("com.squareup.sqldelight:sqljs-driver:$sqlDelightVersion")
            }
        }
        val jsTest by getting
        val androidMain by getting {
            dependencies {
                api("io.ktor:ktor-client-android:$ktorVersion")
                api("com.squareup.sqldelight:android-driver:$sqlDelightVersion")
            }
        }
        val androidTest by getting {
            dependencies {
                api(kotlin("test-junit"))
                api("junit:junit:4.13.2")
            }
        }
        val sqlDriverNativeMain by creating {
            dependsOn(commonMain)
            dependencies {
                api("com.squareup.sqldelight:native-driver:$sqlDelightVersion")
            }
        }
        val iosMain by getting {
            dependsOn(sqlDriverNativeMain)
            dependencies {
                api("io.ktor:ktor-client-ios:$ktorVersion")
            }
        }
        val iosTest by getting
        val nativeMain by getting {
            dependsOn(sqlDriverNativeMain)
            dependencies {
                api("io.ktor:ktor-client-ios:$ktorVersion")
            }
        }
    }

    sqldelight {
        database("AddressDatabase") {
            packageName = "com.library.address.database"
            dialect = "sqlite:3.24"
        }
    }
}

android {
    compileSdkVersion(31)
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdkVersion(24)
        targetSdkVersion(31)
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

tasks {
    register("publishDevFramework") {
        description = "Publish Apple XC Framework to the Cocoa Repo"

        project.exec {
            workingDir = File("$rootDir/../kmp-xcframework-dest")
            commandLine("git", "checkout", "develop").standardOutput
        }

        dependsOn("assemble${xcFrameworkName}DebugXCFramework")

        doLast {

            copy {
                from("$buildDir/XCFrameworks/debug")
                into("$rootDir/../kmp-xcframework-dest")
            }
        }
    }

    register("publishFramework") {
        description = "Publish iOs framework to the Cocoa Repo"

        project.exec {
            workingDir = File("$rootDir/../kmp-xcframework-dest")
            commandLine("git", "checkout", "master").standardOutput
        }

        // Create Release Framework for Xcode
        dependsOn("assemble${xcFrameworkName}ReleaseXCFramework")

        // Replace
        doLast {

            copy {
                from("$buildDir/XCFrameworks/release")
                into("$rootDir/../kmp-xcframework-dest")
            }
        }
    }
}