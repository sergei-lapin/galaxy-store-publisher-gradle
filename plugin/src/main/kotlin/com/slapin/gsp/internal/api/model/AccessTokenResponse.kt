package com.slapin.gsp.internal.api.model

internal data class AccessTokenResponse(
    val ok: Boolean,
    val createdItem: CreatedItem,
) {

  data class CreatedItem(
    val accessToken: String,
  )
}
