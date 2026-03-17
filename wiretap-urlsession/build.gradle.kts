plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.skie)
}

kotlin {
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
            }
        }
    }
}
