plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.androidLint)
    alias(libs.plugins.kover)
}

kotlin {
    android {
        namespace = "dev.skymansandy.wiretap.ktor.api"
        compileSdk {
            version = release(libs.versions.android.compileSdk.get().toInt()) {
                minorApiLevel = 1
            }
        }
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "WiretapKtorApi"
            isStatic = true
        }
    }

    jvm()

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.wiretapCore)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.websockets)
                implementation(libs.koin.core)
            }
        }
    }
}
