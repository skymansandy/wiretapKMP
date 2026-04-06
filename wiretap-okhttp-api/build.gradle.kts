plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.androidLint)
    alias(libs.plugins.kover)
}

kotlin {
    android {
        namespace = "dev.skymansandy.wiretap.okhttp.api"
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
                implementation(projects.wiretapCore)
                implementation(libs.okhttp)
                implementation(libs.koin.core)
            }
        }
    }
}
