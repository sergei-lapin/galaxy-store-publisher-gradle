package com.slapin.gsp.internal.api.model

import com.google.gson.annotations.SerializedName

internal data class SupportedLanguage(
    @SerializedName("languagecode") val languageCode: String? = null,
    val newFeature: String? = null,
    val description: String? = null,
    val appTitle: String? = null,
    val screenshots: List<Screenshot>? = null,
)
