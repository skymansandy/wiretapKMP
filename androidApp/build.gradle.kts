plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xskip-prerelease-check", "-Xexplicit-backing-fields")
    }
}

android {
    namespace = "dev.skymansandy.wiretap"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "dev.skymansandy.wiretapsample"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

// Workaround: androidMultiplatformLibrary doesn't auto-wire Compose resources as Android assets.
// Copy prepared resources from KMP library modules into the app's Android assets with package-qualified paths.
val composeAssetsDir = "${layout.buildDirectory.get()}/generated/composeAssets"

tasks.register<Copy>("copyComposeAppResourcesToAssets") {
    from(project(":composeApp").layout.buildDirectory.dir("generated/compose/resourceGenerator/preparedResources/commonMain/composeResources"))
    into("$composeAssetsDir/composeResources/dev.skymansandy.wiretapsample.resources")
    dependsOn(":composeApp:prepareComposeResourcesTaskForCommonMain")
}
tasks.register<Copy>("copyWiretapCoreResourcesToAssets") {
    from(project(":wiretap-core").layout.buildDirectory.dir("generated/compose/resourceGenerator/preparedResources/commonMain/composeResources"))
    into("$composeAssetsDir/composeResources/dev.skymansandy.wiretap.resources")
    dependsOn(":wiretap-core:prepareComposeResourcesTaskForCommonMain")
}

android.sourceSets["main"].assets.srcDir(composeAssetsDir)

tasks.matching { it.name.startsWith("merge") && it.name.endsWith("Assets") }.configureEach {
    dependsOn("copyComposeAppResourcesToAssets", "copyWiretapCoreResourcesToAssets")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.testExt.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(libs.koin.core)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)
    implementation(libs.androidx.lifecycle.viewmodelCompose)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(projects.composeApp)
    debugImplementation(projects.wiretapKtor)
    debugImplementation(projects.wiretapOkhttp)
    releaseImplementation(projects.wiretapKtorNoop)
    releaseImplementation(projects.wiretapOkhttpNoop)
}