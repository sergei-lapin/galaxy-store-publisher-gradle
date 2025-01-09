package com.slapin.gsp.internal.api

import com.slapin.gsp.internal.api.model.ContentInfo
import com.slapin.gsp.internal.api.model.RegisterBinaryResponse
import com.slapin.gsp.internal.api.model.UploadFileResponse
import com.slapin.gsp.internal.api.model.UploadSession

internal interface SamsungApi {

  fun getContentInfo(
    contentId: String,
  ): ContentInfo

  fun createUploadSession(): UploadSession

  fun uploadBinaryFile(
    filePath: String,
    uploadSession: UploadSession,
  ): UploadFileResponse

  fun registerBinaryFile(
    fileKey: String,
    currentContentInfo: ContentInfo,
  ): RegisterBinaryResponse

  fun submitApp(
    currentContentInfo: ContentInfo,
  ): Boolean

}
