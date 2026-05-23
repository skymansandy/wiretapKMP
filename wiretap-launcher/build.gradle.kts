plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kover)
}

kotlin {
    android {
        namespace = "dev.skymansandy.wiretap.launcher"
        compileSdk {
            version = release(36) {
                minorApiLevel = 1
            }
        }
        minSdk = 24
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "WiretapLauncher"
            isStatic = true
            export(projects.wiretapApi)
            export(projects.wiretapShake)
        }
    }

    jvm()

    sourceSets {
        commonMain {
            dependencies {
                api(projects.wiretapApi)
                implementation(projects.wiretapCore)
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.ui)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.lifecycle.process)
            }
        }

        iosMain {
            dependencies {
                api(projects.wiretapShake)
            }
        }
    }
}
