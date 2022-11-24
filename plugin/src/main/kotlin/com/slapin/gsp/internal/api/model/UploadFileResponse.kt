package com.slapin.gsp.internal.api.model

internal data class UploadFileResponse(
  val fileKey: String,
  val fileName: String,
  val fileSize: Long,
  val errorCode: Int?,
  val errorMsg: String?,
)
