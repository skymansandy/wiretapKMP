plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.androidLint)
    alias(libs.plugins.mokkery)
    alias(libs.plugins.kover)
}

kotlin {
    android {
        namespace = "dev.skymansandy.wiretap.okhttp"
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
                api(projects.wiretapOkhttpApi)
                api(projects.wiretapCore)
                api(libs.okhttp)
                implementation(libs.stately.concurrency)
                implementation(libs.koin.core)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.androidx.startup.runtime)
            }
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.kotest.assertions.core)
            implementation(libs.turbine)
            implementation(libs.okhttp.mockwebserver)
        }
    }
}