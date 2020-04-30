/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */
package com.grab.partner.sdk.models

import com.google.gson.annotations.SerializedName

internal data class ClientPublicInfo(
        @SerializedName("custom_protocols")
        val custom_protocols: List<String>,
        @SerializedName("home_page_url")
        val home_page_url: String,
        @SerializedName("logo_url")
        val logo_url: String,
        @SerializedName("privacy_policy_url")
        val privacy_policy_url: String,
        @SerializedName("product_name")
        val product_name: String,
        @SerializedName("terms_of_service_url")
        val terms_of_service_url: String
)

data class ProtocolInfo(
        @SerializedName("package_adr")
        val package_adr: String,
        @SerializedName("minversion_adr")
        val minversion_adr: String,
        @SerializedName("protocol_adr")
        val protocol_adr: String
)

data class PlaystoreProtocol(
        @SerializedName("appstore_link_adr")
        val appstore_link_adr: String
)