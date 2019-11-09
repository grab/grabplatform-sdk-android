/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk.models

import com.google.gson.annotations.SerializedName

internal data class TokenRequest(
        @SerializedName("code")
        val code: String,
        @SerializedName("client_id")
        val client_id: String,
        @SerializedName("grant_type")
        val grant_type: String,
        @SerializedName("redirect_uri")
        val redirect_uri: String,
        @SerializedName("code_verifier")
        val code_verifier: String
)