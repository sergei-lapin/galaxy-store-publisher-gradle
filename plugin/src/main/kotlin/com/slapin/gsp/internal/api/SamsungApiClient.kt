package com.slapin.gsp.internal.api

import com.google.gson.Gson
import com.slapin.gsp.internal.api.model.*
import com.slapin.gsp.internal.http.asCountingRequestBody
import java.io.File
import java.time.Duration
import java.time.Instant
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.gradle.api.logging.Logger

private val Gson: Gson = Gson().newBuilder().setPrettyPrinting().create()

internal class SamsungApiClient(
  jwtToken: String,
  private val serviceAccountId: String,
  private val logger: Logger,
) : SamsungApi {

  private val accessToken: String
  private val okHttpClient: OkHttpClient

  init {
    logger.lifecycle("Getting access token")
    okHttpClient =
      OkHttpClient.Builder()
        .readTimeout(Duration.ofMinutes(1))
        .writeTimeout(Duration.ofMinutes(20))
        .build()
    accessToken =
      okHttpClient
        .post<AccessTokenResponse>(
          url = "https://devapi.samsungapps.com/auth/accessToken",
          requestBuilder = { commonHeaders(bearerToken = jwtToken) },
        )
        .createdItem
        .accessToken
  }

  override fun getContentInfo(contentId: String): ContentInfo {
    return okHttpClient
      .get<Array<ContentInfo>>(url = "https://devapi.samsungapps.com/seller/contentInfo?contentId=$contentId")
      .first()
  }

  override fun createUploadSession(): UploadSession {
    return okHttpClient
      .post(url = "https://devapi.samsungapps.com/seller/createUploadSessionId")
  }

  override fun uploadBinaryFile(
    filePath: String,
    uploadSession: UploadSession,
  ): UploadFileResponse {
    val file = File(filePath)
    var uploadProgressThreshold = 0
    val requestBody =
      MultipartBody.Builder()
        .addFormDataPart("sessionId", uploadSession.sessionId)
        .addFormDataPart(
          name = "file",
          filename = file.name,
          body =
            file.asCountingRequestBody { written, total ->
              val uploadProgress = written * 100 / total
              if (uploadProgress > uploadProgressThreshold) {
                logger.lifecycle("Uploading — $uploadProgress%")
                uploadProgressThreshold += 5
              }
            },
        )
        .build()
    val start = Instant.now()
    val response =
      okHttpClient.post<UploadFileResponse>(
        url = "https://seller.samsungapps.com/galaxyapi/fileUpload",
        body = requestBody,
      )
    val end = Instant.now()
    val duration = Duration.between(start, end)
    val speedBytes = file.length() / duration.seconds
    val speedFormatted =
      when {
        speedBytes > 1024 * 1024 -> "%.02f MB/s".format(speedBytes / (1024.0 * 1024.0))
        speedBytes > 1024 -> "%.02f KB/s".format(speedBytes / 1024.0)
        else -> "$speedBytes B/s"
      }
    logger.lifecycle(
      "Done in ${duration.toMinutesPart()}m ${duration.toSecondsPart()}s (avg $speedFormatted)"
    )
    response.errorMsg?.let(::error)
    return response
  }

  override fun registerBinaryFile(
    fileKey: String,
    currentContentInfo: ContentInfo,
  ): RegisterBinaryResponse {
    val response =
      okHttpClient
        .post<RegisterBinaryResponse>(
          url = "https://devapi.samsungapps.com/seller/contentUpdate",
          body =
            ContentInfo(
                contentId = currentContentInfo.contentId,
                defaultLanguageCode = currentContentInfo.defaultLanguageCode,
                paid = currentContentInfo.paid,
                usExportLaws = currentContentInfo.usExportLaws,
                ageLimit = currentContentInfo.ageLimit,
                binaryList =
                  listOf(
                    Binary(
                      binarySeq = 1,
                      gms = currentContentInfo.binaryList.firstOrNull()?.gms ?: YesOrNo.No,
                      fileKey = fileKey,
                    ),
                  ),
              )
              .asRequestBody(),
        )
    response.errorMsg?.let(::error)
    response.message?.let(::error)
    return response
  }

  override fun submitApp(
    currentContentInfo: ContentInfo,
  ): Boolean {
    val response =
    okHttpClient
      .post<Boolean>(
        url = "https://devapi.samsungapps.com/seller/contentSubmit",
        body =
        ContentId(contentId = currentContentInfo.contentId).asRequestBody(),
      )
    if (response.not()) {
      logger.error("Failed to submit app")
    }
    return response
  }

  private fun Any.asRequestBody(): RequestBody {
    return Gson.toJson(this).toRequestBody("application/json".toMediaType())
  }

  private inline fun <reified T : Any> OkHttpClient.post(
    url: String,
    body: RequestBody = Unit.asRequestBody(),
    requestBuilder: Request.Builder.() -> Request.Builder = { commonHeaders() },
  ): T {
    val request =
      Request.Builder()
        .url(url)
        .header("Accept", "application/json")
        .header("content-type", requireNotNull(body.contentType()).toString())
        .requestBuilder()
        .post(body)
        .build()
    return newCall(request).execute().use { response ->
      //submit-app don't have response body so only return boolean with successful status
      when (T::class) {
        Boolean::class -> response.isSuccessful as T
        else -> response.body.fromJson()
      }
    }
  }

  private inline fun <reified T : Any> OkHttpClient.get(
    url: String,
    requestBuilder: Request.Builder.() -> Request.Builder = { commonHeaders() },
  ): T {
    val request = Request.Builder().url(url).get().requestBuilder().build()
    return newCall(request).execute().use { response -> response.body.fromJson() }
  }

  private inline fun <reified T : Any> ResponseBody?.fromJson(): T {
    this ?: error("null response body")
    val rawJson = string()
    return runCatching { Gson.fromJson(rawJson, T::class.java) }
      .onFailure {
        logger.error("Error parsing response:")
        logger.error(rawJson)
      }
      .getOrThrow()
  }

  private fun Request.Builder.commonHeaders(
    bearerToken: String = accessToken,
  ): Request.Builder {
    return header("Authorization", "Bearer $bearerToken")
      .header("service-account-id", serviceAccountId)
  }
}
