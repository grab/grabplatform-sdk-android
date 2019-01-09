/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk.models

internal data class TokenAPIResponse(
        val access_token: String?="",
        val token_type: String?="",
        val expires_in: String?= "",
        val id_token: String?="",
        val refresh_token: String?=""
)