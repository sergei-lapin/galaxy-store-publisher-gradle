package com.slapin.gsp.task

import com.slapin.gsp.internal.api.SamsungApiClient
import com.slapin.gsp.internal.key.getJWTToken
import com.slapin.gsp.internal.retry.withRetry
import java.io.File
import java.nio.file.Files
import java.time.Duration
import javax.inject.Inject
import kotlin.io.path.extension
import kotlin.io.path.nameWithoutExtension
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.kotlin.dsl.property
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault
abstract class PublishToGalaxyStore
@Inject
constructor(
  objectFactory: ObjectFactory,
  private val providerFactory: ProviderFactory,
) : DefaultTask() {

  @get:Input abstract val serviceAccountId: Property<String>

  @get:Input abstract val serviceAccountScopes: ListProperty<String>

  @get:InputFile @get:Optional abstract val serviceAccountKeyFile: RegularFileProperty

  @get:Input abstract val appContentId: Property<String>

  @Option(
    option = "apkDirPath",
    description = "Directory with APK to upload (variant output directory by default)",
  )
  @get:Internal
  val apkDirPath: Property<String> = objectFactory.property()

  @Option(
    option = "bundleDirPath",
    description = "Directory with AAB to upload (variant output directory by default)",
  )
  @get:Internal
  val bundleDirPath: Property<String> = objectFactory.property()

  @get:Internal
  val privateKey: Provider<String>
    get() =
      serviceAccountKeyFile
        .map(RegularFile::getAsFile)
        .map(File::readText)
        .orElse(providerFactory.environmentVariable("SAMSUNG_SERVICE_ACCOUNT_KEY"))

  private fun findBinary(): java.nio.file.Path {
    if (bundleDirPath.isPresent) {
      val bundleDir = File(bundleDirPath.get())
      if (bundleDir.isDirectory) {
        val aab = bundleDir.listFiles()?.firstOrNull { it.extension == "aab" }
        if (aab != null) {
          logger.lifecycle("Found AAB: ${aab.name}")
          return aab.toPath()
        }
      }
    }
    if (apkDirPath.isPresent) {
      val apkDir = File(apkDirPath.get())
      if (apkDir.isDirectory) {
        val apk = apkDir.listFiles()?.firstOrNull { it.extension == "apk" }
        if (apk != null) {
          logger.lifecycle("Found APK: ${apk.name}")
          return apk.toPath()
        }
      }
    }
    val searched = buildList {
      if (bundleDirPath.isPresent) add("AAB in ${bundleDirPath.get()}")
      if (apkDirPath.isPresent) add("APK in ${apkDirPath.get()}")
    }
    error("No app binary (AAB or APK) found. Searched: ${searched.joinToString("; ")}")
  }

  @TaskAction
  fun run() {

    logger.lifecycle("Searching for app binary")

    val binary = findBinary()

    logger.lifecycle("Generating JWT token")

    val jwtToken =
      getJWTToken(
        privateKey = privateKey.get(),
        serviceAccountId = serviceAccountId.get(),
        scopes = serviceAccountScopes.get(),
      )

    val samsungApiClient =
      SamsungApiClient(
        jwtToken = jwtToken,
        serviceAccountId = serviceAccountId.get(),
        logger = logger,
      )

    logger.lifecycle("Getting current content info")

    val currentContentInfo = samsungApiClient.getContentInfo(appContentId.get())

    logger.lifecycle("Establishing upload session")

    val uploadSession = samsungApiClient.createUploadSession()
    // Galaxy Store fails upload with the same name even though versionName/versionCode updated
    val fixedBinaryName =
      "${binary.nameWithoutExtension.replace('.', '_')}-${System.currentTimeMillis()}.${binary.extension}"
    val fixedBinary = Files.copy(binary, binary.resolveSibling(fixedBinaryName))

    logger.lifecycle("Uploading $fixedBinaryName")

    val uploadFileResponse =
      samsungApiClient.uploadBinaryFile(
        filePath = fixedBinary.toString(),
        uploadSession = uploadSession,
      )

    logger.lifecycle("Cleaning up temporary binary")

    Files.delete(fixedBinary)

    logger.lifecycle("Registering new binary")

    withRetry(
      delay = Duration.ofSeconds(10),
      count = 5,
      onFailure = { error("Failed to register new binary") }
    ) {
      samsungApiClient.registerBinaryFile(
        fileKey = uploadFileResponse.fileKey,
        currentContentInfo = currentContentInfo,
      )
      logger.lifecycle("Successfully registered new binary")
    }
  }
}
