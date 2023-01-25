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

  @get:Internal
  val privateKey: Provider<String>
    get() =
      serviceAccountKeyFile
        .map(RegularFile::getAsFile)
        .map(File::readText)
        .orElse(providerFactory.environmentVariable("SAMSUNG_SERVICE_ACCOUNT_KEY"))

  @TaskAction
  fun run() {

    logger.lifecycle("Searching for APK")

    val apk =
      File(apkDirPath.get()).listFiles()?.firstOrNull { it.extension == "apk" }?.toPath()
        ?: error("No APK found in ${apkDirPath.get()}")

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
    val fixedApkName =
      "${apk.nameWithoutExtension.replace('.', '_')}-${System.currentTimeMillis()}.${apk.extension}"
    val fixedApk = Files.copy(apk, apk.resolveSibling(fixedApkName))

    logger.lifecycle("Uploading $fixedApkName")

    val uploadFileResponse =
      samsungApiClient.uploadBinaryFile(
        filePath = fixedApk.toString(),
        uploadSession = uploadSession,
      )

    logger.lifecycle("Cleaning up temporary binary")

    Files.delete(fixedApk)

    logger.lifecycle("Registering new binary")

    withRetry(
      delay = Duration.ofSeconds(10),
      count = 5,
      onFailure = { logger.error("Failed to register new binary") }
    ) {
      samsungApiClient.registerBinaryFile(
        fileKey = uploadFileResponse.fileKey,
        currentContentInfo = currentContentInfo,
      )
      logger.lifecycle("Successfully registered new binary")
    }
  }
}
