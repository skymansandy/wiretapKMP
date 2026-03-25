plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.androidLint) apply false
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.skie) apply false
    alias(libs.plugins.kmmbridge) apply false
    alias(libs.plugins.mokkery) apply false
    alias(libs.plugins.kover)
    alias(libs.plugins.mavenPublish) apply false
    alias(libs.plugins.bcv) apply false
}

dependencies {
    listOf(
        "wiretap-core",
        "wiretap-ktor",
        "wiretap-ktor-noop",
        "wiretap-okhttp",
        "wiretap-okhttp-noop",
        "wiretap-urlsession",
        "wiretap-shake",
    ).forEach { kover(project(":$it")) }
}

kover {
    merge {
        allProjects {
            it.name in listOf(
                "wiretap-core",
                "wiretap-ktor",
                "wiretap-ktor-noop",
                "wiretap-okhttp",
                "wiretap-okhttp-noop",
                "wiretap-urlsession",
                "wiretap-shake",
            )
        }
        createVariant("jvmCoverage") {
            add("jvm", optional = true)
        }
    }
}

val publishableModules = setOf(
    "wiretap-core",
    "wiretap-ktor",
    "wiretap-ktor-noop",
    "wiretap-okhttp",
    "wiretap-okhttp-noop",
    "wiretap-urlsession",
    "wiretap-shake",
)

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    if (name in publishableModules) {
        val wiretapGroup = findProperty("wiretap.group") as String
        val wiretapVersion = findProperty("wiretap.version") as String

        apply(plugin = "com.vanniktech.maven.publish")
        apply(plugin = "org.jetbrains.kotlinx.binary-compatibility-validator")

        afterEvaluate {
            tasks.withType<Sign>().configureEach {
                isEnabled = !gradle.startParameter.taskNames.any { it.contains("MavenLocal", ignoreCase = true) }
            }
        }

        extensions.configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
            coordinates(wiretapGroup, name, wiretapVersion)
            publishToMavenCentral(automaticRelease = true)
            signAllPublications()

            pom {
                name.set("WiretapKMP")
                description.set("Kotlin Multiplatform network inspection and mocking SDK")
                url.set("https://github.com/skymansandy/wiretapKMP")
                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
                developers {
                    developer {
                        id.set("skymansandy")
                        name.set("skymansandy")
                        email.set("iamsandythedev@gmail.com")
                    }
                }
                scm {
                    url.set("https://github.com/skymansandy/wiretapKMP")
                    connection.set("scm:git:git://github.com/skymansandy/wiretapKMP.git")
                    developerConnection.set("scm:git:ssh://github.com/skymansandy/wiretapKMP.git")
                }
            }
        }
    }

    configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        buildUponDefaultConfig = true
        autoCorrect = true
        config.setFrom(rootProject.files("detekt.yml"))
    }

    dependencies {
        "detektPlugins"("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.7")
    }

    afterEvaluate {
        val kmpSourceDirs = fileTree("src") {
            include("*Main/kotlin/**/*.kt", "*Test/kotlin/**/*.kt")
        }
        if (!kmpSourceDirs.isEmpty) {
            tasks.register<io.gitlab.arturbosch.detekt.Detekt>("detektAll") {
                setSource(files("src"))
                include("**/*.kt")
                exclude("**/build/**")
                config.setFrom(rootProject.files("detekt.yml"))
                buildUponDefaultConfig = true
                autoCorrect = true
                if (file("detekt-baseline.xml").exists()) {
                    baseline.set(file("detekt-baseline.xml"))
                }
            }

            tasks.register<io.gitlab.arturbosch.detekt.DetektCreateBaselineTask>("detektBaselineAll") {
                setSource(files("src"))
                include("**/*.kt")
                exclude("**/build/**")
                config.setFrom(rootProject.files("detekt.yml"))
                buildUponDefaultConfig = true
                baseline.set(file("detekt-baseline.xml"))
            }
        }
    }
}
