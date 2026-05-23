plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kover)
}

kotlin {
    android {
        namespace = "dev.skymansandy.wiretap.launcher.noop"
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
            baseName = "WiretapLauncherNoop"
            isStatic = true
            export(projects.wiretapApi)
        }
    }

    jvm()

    sourceSets {
        commonMain {
            dependencies {
                api(projects.wiretapApi)
            }
        }
    }
}
