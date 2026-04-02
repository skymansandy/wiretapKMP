plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.androidLint)
    alias(libs.plugins.skie)
    alias(libs.plugins.mokkery)
    alias(libs.plugins.kover)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xskip-prerelease-check")
    }

    android {
        namespace = "dev.skymansandy.wiretap.ktor"
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
            baseName = "WiretapKtor"
            export(projects.wiretapCore)
            isStatic = true
        }
    }

    jvm()

    sourceSets {
        commonMain {
            dependencies {
                api(projects.wiretapCore)
                api(libs.ktor.client.core)
                implementation(libs.ktor.client.websockets)
                implementation(libs.stately.concurrency)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.ktor.client.android)
            }
        }

        iosMain {
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }

        jvmMain.dependencies {
            implementation(libs.ktor.client.java)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.kotest.assertions.core)
            implementation(libs.turbine)
            implementation(libs.ktor.client.mock)
        }
    }
}
