plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kover)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "WiretapURLSession"
            isStatic = true
        }
    }

    sourceSets {
        iosMain {
            dependencies {
                implementation(libs.koin.core)
                implementation(libs.compose.runtime)
                implementation(projects.wiretapCore)
            }
        }
    }
}
