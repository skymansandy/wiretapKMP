plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.swiftklib)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.compilations {
            val main by getting {
                cinterops {
                    create("SwiftShakeDetector")
                }
            }
        }
    }

    sourceSets {
        iosMain {
            dependencies {
            }
        }
    }
}

swiftklib {
    create("SwiftShakeDetector") {
        path = file("../SwiftShakeDetector")
        packageName("dev.skymansandy.wiretap.shake")
    }
}
