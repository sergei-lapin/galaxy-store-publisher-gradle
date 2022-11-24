@file:Suppress("UnstableApiUsage")

pluginManagement {
  includeBuild("plugin")
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
  }
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
  }
}

include(":sample")

rootProject.name = "galaxy-store-publisher-gradle"
