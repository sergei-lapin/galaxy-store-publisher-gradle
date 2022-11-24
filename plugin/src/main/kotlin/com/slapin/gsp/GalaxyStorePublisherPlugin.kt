package com.slapin.gsp

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.gradle.internal.tasks.BundleToApkTask
import com.slapin.gsp.task.PublishToGalaxyStore
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

abstract class GalaxyStorePublisherPlugin : Plugin<Project> {

  override fun apply(target: Project) {
    target.plugins.withId("com.android.application") { target.applyGalaxyStorePublisherPlugin() }
  }

  private fun Project.applyGalaxyStorePublisherPlugin() {
    val baseExtension = extensions.create<GalaxyStorePublisherExtension>("galaxyStorePublisher")
    val componentsExtension = extensions.getByType<ApplicationAndroidComponentsExtension>()
    componentsExtension.beforeVariants { variantBuilder ->
      variantBuilder.registerExtension(
        GalaxyStorePublisherExtension::class.java,
        objects.newInstance(),
      )
    }
    componentsExtension.onVariants { variant ->
      val variantTaskName = variant.name.replaceFirstChar(Char::uppercaseChar)
      val variantExtension =
        requireNotNull(variant.getExtension(GalaxyStorePublisherExtension::class.java))
      tasks.register<PublishToGalaxyStore>("publish${variantTaskName}ToGalaxyStore") {
        appContentId.set(variantExtension.appContentId.orElse(baseExtension.appContentId))
        serviceAccountId.set(
          variantExtension.serviceAccountId.orElse(baseExtension.serviceAccountId)
        )
        serviceAccountScopes.set(
          variantExtension.serviceAccountScopes.orElse(baseExtension.serviceAccountScopes)
        )
        serviceAccountKeyFile.set(
          variantExtension.serviceAccountKeyFile.orElse(baseExtension.serviceAccountKeyFile)
        )
        val variantApkDirPath = buildString {
          append("outputs/apk")
          variant.flavorName?.let { append("/$it") }
          variant.buildType?.let { append("/$it") }
        }
        apkDirPath.set(layout.buildDirectory.dir(variantApkDirPath).map { it.asFile.path })
        group = "Galaxy Store Publisher"
        description = "Publish ${variant.name} APK to Samsung Galaxy Store"
      }
    }
  }
}
