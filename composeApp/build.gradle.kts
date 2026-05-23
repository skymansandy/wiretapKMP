import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ktorfit)
}

// FIXME: Temporary, remove after a compatible ktorfit will be released with KSP support
ktorfit {
    compilerPluginVersion.set("-")
}

enum class BuildVariant { Dev, Prod }

val buildVariant: BuildVariant = when (providers.gradleProperty("wiretapVariant").orNull?.lowercase()) {
    "prod" -> BuildVariant.Prod
    else -> BuildVariant.Dev
}

val wiretapKtorDependency = when (buildVariant) {
    BuildVariant.Prod -> projects.wiretapKtorNoop
    BuildVariant.Dev -> projects.wiretapKtor
}

kotlin {
    android {
        namespace = "dev.skymansandy.wiretapsample"
        compileSdk { version = release(libs.versions.android.compileSdk.get().toInt()) }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "WiretapSample"
            isStatic = true
        }
    }

    jvm()

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.android)
            // wiretapKtorDependency intentionally omitted: androidApp supplies it via
            // debug/releaseImplementation so the AS Build Variant selector drives the swap.
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.material.icons.extended)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.websockets)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktorfit.lib)
            implementation(libs.kotlinx.coroutines.core)
            compileOnly(wiretapKtorDependency)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.ktor.client.java)
            implementation(wiretapKtorDependency)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(wiretapKtorDependency)
        }
    }
}

compose.resources {
    packageOfResClass = "dev.skymansandy.wiretapsample.resources"
    generateResClass = always
}

compose.desktop {
    application {
        mainClass = "dev.skymansandy.wiretapsample.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "dev.skymansandy.wiretapsample"
            packageVersion = "1.0.0"
        }
    }
}
