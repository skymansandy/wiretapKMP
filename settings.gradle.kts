rootProject.name = "WiretapKMP"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        mavenLocal()
        maven {
            url = uri("https://maven.pkg.github.com/skymansandy/jsonCMP")
            credentials {
                username = providers.gradleProperty("gpr.user").orNull ?: System.getenv("GH_USERNAME")
                password = providers.gradleProperty("gpr.key").orNull ?: System.getenv("GH_TOKEN")
            }
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":androidApp")
include(":composeApp")
include(":wiretap-core")
include(":wiretap-ktor")
include(":wiretap-ktor-noop")
include(":wiretap-okhttp")
include(":wiretap-okhttp-noop")
include(":wiretap-urlsession")
include(":wiretap-urlsession-noop")
include(":wiretap-shake")
