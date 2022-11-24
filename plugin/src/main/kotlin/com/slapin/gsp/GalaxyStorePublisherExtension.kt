package com.slapin.gsp

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

abstract class GalaxyStorePublisherExtension {

  /**
   * Samsung Service Account ID (can be obtained at
   * [Samsung API Console](https://apiconsole.samsungapps.com/))
   */
  abstract val serviceAccountId: Property<String>

  /**
   * Samsung Service Account ID scopes (specified while issuing service account at
   * [Samsung API Console](https://apiconsole.samsungapps.com/))
   */
  abstract val serviceAccountScopes: ListProperty<String>

  /**
   * If not specified — plugin will look for private key in `SAMSUNG_SERVICE_ACCOUNT_KEY` env var
   */
  abstract val serviceAccountKeyFile: RegularFileProperty

  /**
   * Target application Content ID (can be found at
   * [Samsung Seller Portal](https://seller.samsungapps.com/content/common/summaryContentList.as))
   */
  abstract val appContentId: Property<String>
}
