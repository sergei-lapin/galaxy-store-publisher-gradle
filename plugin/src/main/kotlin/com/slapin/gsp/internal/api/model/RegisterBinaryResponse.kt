package com.slapin.gsp.internal.api.model

import com.google.gson.annotations.SerializedName

internal data class RegisterBinaryResponse(
    @SerializedName("ctntId") val contentId: String,
    val contentStatus: ContentInfo.Status,
    val httpStatus: String,
    val errorCode: Int?,
    val errorMsg: String?,
    val message: String?, // on code 500 they send message instead of errorMsg
)
