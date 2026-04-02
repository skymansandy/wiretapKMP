plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kmmbridge)
    alias(libs.plugins.mokkery)
    alias(libs.plugins.kover)
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
        }
    }

    sourceSets {
        iosMain {
            dependencies {
                api(projects.wiretapCore)
                implementation(libs.stately.concurrency)
                implementation(libs.koin.core)
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

kmmbridge {
    mavenPublishArtifacts()
    spm()
}
