/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk.models

import com.google.gson.annotations.SerializedName

internal data class TokenAPIResponse(
        @SerializedName("access_token")
        val access_token: String?="",
        @SerializedName("token_type")
        val token_type: String?="",
        @SerializedName("expires_in")
        val expires_in: String?= "",
        @SerializedName("id_token")
        val id_token: String?="",
        @SerializedName("refresh_token")
        val refresh_token: String?=""
)