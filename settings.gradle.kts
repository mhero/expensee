pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Expensee"

include(":app")

include(":core:common")
include(":core:database")
include(":core:network")
include(":core:security")
include(":core:ui")
include(":core:testing")
include(":core:datastore")

include(":feature:auth")
include(":feature:expenses")
include(":feature:categories")
include(":feature:dashboard")
include(":feature:settings")
