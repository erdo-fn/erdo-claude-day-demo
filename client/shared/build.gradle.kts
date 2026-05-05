import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerializationPlugin)
}

kotlin {

    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.jvm.toolchain.get().toInt()))
    }

    targets.withType<org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget> {
        compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.fromTarget(libs.versions.jvm.target.get()))
                }
            }
        }
    }


    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.fromTarget(libs.versions.jvm.target.get()))
                }
            }
        }
    }

    val xcFramework = XCFramework()
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true

            //until we use the XCFramework from fore itself?
            export(libs.fore.core)
            export(libs.fore.net)
            export(libs.fore.compose)
            export(libs.koin.core)
            export(libs.koin.compose)
            export(libs.persista)
            export(libs.kotlinx.serialization)

            xcFramework.add(this)
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.fore.core)
            api(libs.fore.net)
            api(libs.fore.compose)
            api(libs.koin.core)
            api(libs.koin.compose)
            api(libs.persista)
            api(libs.bundles.ktor)
            api(libs.kotlinx.serialization)
        }
        androidMain.dependencies {
            api(libs.ktor.client.cio)
        }
        iosMain.dependencies {
            api(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            api(libs.fore.test.fixtures)
        }
    }
}

android {
    namespace = "com.deleteme.kmpfoo"
    compileSdk = 36
    defaultConfig {
        minSdk = 23
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}


//   Build the XCFramework and copy it over to the iosApp
//
//   ./gradlew :shared:prepAllXCFrameworkForXcode
//
//   then from XCode, add framework
//   you might need JDK specified explicitly in gradle.properties org.gradle.java.home=/Applications/Android Studio.app/Contents/jbr/Contents/Home

fun prepXCFrameworkForXcode(config: String) = tasks.registering(Sync::class) {
    from(layout.buildDirectory.dir("XCFrameworks/$config/shared.xcframework"))
    into(rootProject.layout.projectDirectory.dir("iosApp/Frameworks/shared.$config.xcframework"))
    dependsOn("assembleXCFramework")
}
val prepDebugXCFrameworkForXcode by prepXCFrameworkForXcode("debug")
val prepReleaseXCFrameworkForXcode by prepXCFrameworkForXcode("release")

tasks.register("prepAllXCFrameworkForXcode") {
    dependsOn(prepDebugXCFrameworkForXcode, prepReleaseXCFrameworkForXcode)
}
