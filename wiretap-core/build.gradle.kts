plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.androidLint)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.skie)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.mokkery)
    alias(libs.plugins.kover)
}

kotlin {
    android {
        namespace = "dev.skymansandy.wiretap"
        compileSdk {
            version = release(libs.versions.android.compileSdk.get().toInt()) {
                minorApiLevel = 1
            }
        }
        minSdk = libs.versions.android.minSdk.get().toInt()
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
                api(projects.wiretapApi)
                implementation(libs.kotlin.stdlib)
                implementation(libs.jsonCmp)
                implementation(libs.koin.core)
                implementation(libs.koin.compose)
                implementation(libs.koin.compose.viewmodel)
                api(libs.kotlinx.coroutines.core)
                implementation(libs.stately.concurrent.collections)

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
                implementation(libs.navigation3.ui)
                implementation(libs.lifecycle.viewmodel.navigation3)
                implementation(libs.kotlinx.serialization.core)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotest.framework.engine)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.kotest.assertions.core)
                implementation(libs.turbine)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.kotest.runner.junit5)
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

tasks.withType<Test>().configureEach { useJUnitPlatform() }
