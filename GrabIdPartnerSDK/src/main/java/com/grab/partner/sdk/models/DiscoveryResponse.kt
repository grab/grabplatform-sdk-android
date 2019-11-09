/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk.models

import com.google.gson.annotations.SerializedName

internal data class DiscoveryResponse(
        @SerializedName("authorization_endpoint")
        val authorization_endpoint: String? = "",
        @SerializedName("token_endpoint")
        val token_endpoint: String? = "",
        @SerializedName("id_token_verification_endpoint")
        val id_token_verification_endpoint: String? = "",
        @SerializedName("client_public_info_endpoint")
        var client_public_info_endpoint: String = ""
)