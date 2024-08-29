pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "jeminai"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include(":core:testing")
include(":core:data")
include(":core:datastore")
include(":core:datastore-proto")
include(":core:model")
include(":core:dispatchers")

include(":feature:settings")

include(":app")