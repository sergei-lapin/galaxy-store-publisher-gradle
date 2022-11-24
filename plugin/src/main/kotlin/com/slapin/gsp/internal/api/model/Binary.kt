package com.slapin.gsp.internal.api.model

import com.google.gson.annotations.SerializedName

internal data class Binary(
    @SerializedName("apimaxSdkVersion") val apiMaxSdkVersion: Int? = null,
    @SerializedName("apiminSdkVersion") val apiMinSdkVersion: Int? = null,
    val binarySeq: Int? = null,
    @SerializedName("filekey") val fileKey: String? = null,
    val fileName: String? = null,
    val gms: YesOrNo? = null,
    val iapSdk: YesOrNo? = null,
    val nativePlatforms: String? = null,
    val packageName: String? = null,
    val versionCode: String? = null,
    val versionName: String? = null,
)
