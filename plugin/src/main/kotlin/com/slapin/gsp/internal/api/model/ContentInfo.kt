package com.slapin.gsp.internal.api.model

import com.google.gson.annotations.SerializedName

internal data class ContentInfo(
    val contentId: String = "",
    val appTitle: String = "",
    val defaultLanguageCode: String = "",
    val contentStatus: Status? = null,
    val applicationType: String? = null,
    val longDescription: String? = null,
    val shortDescription: String? = null,
    val newFeature: String? = null,
    val ageLimit: String? = null,
    @SerializedName("openSourceURL") val openSourceUrl: String? = null,
    @SerializedName("privatePolicyURL") val privatePolicyUrl: String? = null,
    @SerializedName("youTubeURL") val youtubeUrl: String? = null,
    val copyrightHolder: String? = null,
    @SerializedName("supportEMail") val supportEmail: String? = null,
    val supportedSiteUrl: String? = null,
    val paid: YesOrNo = YesOrNo.No,
    val binaryList: List<Binary> = emptyList(),
    val standardPrice: String? = null,
    val autoAddCountry: Boolean? = null,
    val publicationType: String? = null,
    val startPublicationDate: String? = null,
    val stopPublicationDate: String? = null,
    val usExportLaws: Boolean? = null,
    val reviewComment: String? = null,
    @SerializedName("reviewFilename") val reviewFileName: String? = null,
    @SerializedName("reviewFilekey") val reviewFileKey: String? = null,
    @SerializedName("edgescreen") val edgeScreen: String? = null,
    @SerializedName("edgescreenKey") val edgeScreenKey: String? = null,
    @SerializedName("edgescreenplus") val edgeScreenPlus: String? = null,
    @SerializedName("edgescreenplusKey") val edgeScreenPlusKey: String? = null,
    val sellCountryList: List<SellCountry>? = null,
    val supportedLanguages: List<String>? = null,
    val addLanguage: List<SupportedLanguage>? = null,
    val screenshots: List<Screenshot>? = null,
    val category: List<Category>? = null,
    val heroImage: String? = null,
    val heroImageKey: String? = null,
) {

  enum class Status {
    @SerializedName("FOR_SALE") ForSale,
    @SerializedName("REGISTERING") Registering,
    @SerializedName("REGISTER_COMPLETE") RegisterComplete,
    @SerializedName("READY_FOR_CHANGE") ReadyForChange,
    @SerializedName("READY_FOR_SALE") ReadyForSale,
    @SerializedName("SUSPENDED") Suspended,
    @SerializedName("TERMINATED") Terminated,
  }
}
