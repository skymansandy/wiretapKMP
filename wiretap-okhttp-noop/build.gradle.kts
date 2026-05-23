plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kover)
}

kotlin {
    android {
        namespace = "dev.skymansandy.wiretap.okhttp.noop"
        compileSdk {
            version = release(36) {
                minorApiLevel = 1
            }
        }
        minSdk = 24
    }

    jvm()

    sourceSets {
        commonMain {
            dependencies {
                api(projects.wiretapApi)
                api(projects.wiretapLauncherNoop)
                implementation(libs.okhttp)
                implementation(libs.okhttp.sse)
            }
        }
    }
}
