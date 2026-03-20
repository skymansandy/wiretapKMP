plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kmmbridge)
    alias(libs.plugins.mokkery)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xskip-prerelease-check")
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "WiretapURLSession"
            isStatic = true
            export(projects.wiretapCore)
            linkerOpts("-lsqlite3")
        }
        iosTarget.binaries.all {
            linkerOpts("-lsqlite3")
        }
    }

    sourceSets {
        iosMain {
            dependencies {
                api(projects.wiretapCore)
            }
        }

        iosTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.kotest.assertions.core)
                implementation(libs.turbine)
            }
        }
    }
}

group = "dev.skymansandy"
version = "0.1.0"

kmmbridge {
    mavenPublishArtifacts()
    spm()
}
