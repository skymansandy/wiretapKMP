plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kover)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xexplicit-backing-fields")
    }

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
                implementation(libs.okhttp)
                implementation(libs.koin.core)
                implementation(libs.compose.runtime)
                implementation(projects.wiretapCore)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.androidx.activity.compose)
            }
        }
    }
}
