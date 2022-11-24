# Galaxy Store Publisher

[![Plugin](https://img.shields.io/maven-metadata/v?label=Gradle%20Plugin&logo=Gradle&metadataUrl=https%3A%2F%2Fplugins.gradle.org%2Fm2%2Fcom%2Fsergei-lapin%2Fgalaxy-store-publisher%2Fcom.sergei-lapin.galaxy-store-publisher.gradle.plugin%2Fmaven-metadata.xml)](https://plugins.gradle.org/plugin/com.sergei-lapin.galaxy-store-publisher)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Gradle Plugin that lets you publish Android applications to the Galaxy Store.  
Plugin uses [Galaxy Store Developer API](https://developer.samsung.com/galaxy-store/galaxy-store-developer-api.html)

## Prerequisites

In order to use [Galaxy Store Developer API](https://developer.samsung.com/galaxy-store/galaxy-store-developer-api.html)
you need
to [create a service account](https://developer.samsung.com/galaxy-store/galaxy-store-developer-api/create-an-access-token.html#Create-a-service-account)
and supply its credentials to the plugin.

## Setup

In you android application module apply plugin

```kotlin
plugins {
    id("com.android.application")
    id("com.sergei-lapin.galaxy-store-publisher") version "{latest-version}"
}
```

> **Supported AGP versions**
> Plugin supports only AGP 7.+

Now you can configure credentials like this

```kotlin
galaxyStorePublisher {
    appContentId.set("app-content-id")
    serviceAccountId.set("service-account-id")
    serviceAccountScopes.set(listOf("publishing"))
}
```

As for service account key it can be provided in two ways: either by setting it via Gradle extension

```kotlin
galaxyStorePublisher {
    serviceAccountKeyFile.set(file("your-private-key-file-location"))
}
```

or by setting up `SAMSUNG_SERVICE_ACCOUNT_KEY` environment variable

### Per-variant setup

You can override global configuration and for some application variant set up its own set of properties like this

```kotlin
androidComponents.onVariants { variant ->
    val variantExtension =
        requireNotNull(variant.getExtension(GalaxyStorePublisherExtension::class.java))
    if (variant.name == "release") {
        variantExtension.appContentId.set("app-content-id-override")
    }
}
```

## Publishing

Once setup is finished a number of tasks will be registered, e.g. if you have only `debug` and `release` variants of your 
application, there will be two tasks `publishDebugToGalaxyStore` and `publishReleaseToGalaxyStore`.  

You can list all registered tasks by running `./gradlew :your-app-module:tasks --group="Galaxy Store Publisher"`.  

Registered tasks are not triggering corresponding apk assembling, so you'd need to assemble target apk prior to 
publishing task invocation.  
This is by design, as this plugin is intended to act as a lightweight CLI.