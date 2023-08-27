plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("org.jetbrains.compose")
}

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
kotlin {
    targetHierarchy.default()

    androidTarget()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        version = "1.0"
        ios.deploymentTarget = "14.1"
        podfile = project.file("../iosApp/Podfile")


        pod("FirebaseCore")
        pod("FirebaseFirestore")
        pod("WebRTC-SDK") {
            version = "114.5735.02"
            linkOnly = true
        }

        podfile = project.file("../iosApp/Podfile")

        framework {
            baseName = "shared"
            export(libs.webrtc.kmp)
        }

        xcodeConfigurationToNativeBuildType["CUSTOM_DEBUG"] = org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType.DEBUG
        xcodeConfigurationToNativeBuildType["CUSTOM_RELEASE"] = org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType.RELEASE
    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.webrtc.kmp)
                implementation(libs.kotlinX.coroutines)

                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.animation)
                implementation(compose.material)

                api(libs.precompose)
                api(libs.precompose.viewmodel)

                api(libs.koin.core)

                implementation(libs.kermit)

                api(libs.permissions)
                api(libs.permissions.compose) // permissions api + compose extensions

            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

android {
    namespace = "fr.yudo.webrtcpoc"
    compileSdk = 33
    defaultConfig {
        minSdk = 28
    }
}