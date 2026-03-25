plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.androidLint)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.skie)
    alias(libs.plugins.mokkery)
    alias(libs.plugins.kover)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexplicit-backing-fields")
    }

    android {
        namespace = "dev.skymansandy.wiretap"
        compileSdk {
            version = release(36) {
                minorApiLevel = 1
            }
        }
        minSdk = 24

        withDeviceTestBuilder {
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "WiretapKit"
            isStatic = true
            linkerOpts("-lsqlite3")
            export(projects.wiretapShake)
        }
        iosTarget.binaries.all {
            linkerOpts("-lsqlite3")
        }
    }

    jvm()

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)
                api(libs.jsonCmp)
                api(libs.koin.core)
                api(libs.koin.compose)
                api(libs.kotlinx.coroutines.core)

                implementation(libs.room.runtime)
                implementation(libs.sqlite.bundled)
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.ui)
                implementation(libs.compose.uiToolingPreview)
                implementation(libs.compose.components.resources)
                implementation(libs.material.icons.extended)
                implementation(libs.androidx.lifecycle.viewmodelCompose)
                implementation(libs.androidx.lifecycle.runtimeCompose)
                implementation(libs.cashapp.paging.common)
                implementation(libs.cashapp.paging.compose)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.kotest.assertions.core)
                implementation(libs.turbine)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.androidx.startup.runtime)
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.core.ktx)
                implementation(libs.androidx.lifecycle.process)
            }
        }

        getByName("androidDeviceTest") {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.androidx.runner)
                implementation(libs.androidx.core)
                implementation(libs.androidx.testExt.junit)
                implementation(libs.androidx.compose.ui.test.junit4)
                implementation(libs.androidx.compose.ui.test.manifest)
            }
        }

        iosMain {
            dependencies {
                api(projects.wiretapShake)
            }
        }
    }
}

compose.resources {
    packageOfResClass = "dev.skymansandy.wiretap.resources"
    generateResClass = always
}

// Workaround: Compose resources plugin doesn't configure outputDirectory for androidDeviceTest
gradle.taskGraph.whenReady {
    allTasks.filter { it.name == "copyAndroidDeviceTestComposeResourcesToAndroidAssets" }.forEach { task ->
        val outputDir = task.property("outputDirectory") as? org.gradle.api.file.DirectoryProperty
        if (outputDir != null && !outputDir.isPresent) {
            outputDir.set(layout.buildDirectory.dir("intermediates/compose-resources/androidDeviceTest/assets"))
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspAndroid", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
    add("kspJvm", libs.room.compiler)
}
