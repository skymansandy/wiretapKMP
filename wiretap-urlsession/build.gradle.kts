plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kmmbridge)
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
            linkerOpts("-lsqlite3")
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

group = "dev.skymansandy"
version = "0.1.0"

kmmbridge {
    mavenPublishArtifacts()
    spm()
}
