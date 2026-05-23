plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kover)
}

kotlin {
    android {
        namespace = "dev.skymansandy.wiretap.launcher.noop"
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
