/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk.models

internal data class TokenRequest(
        val code: String,
        val client_id: String,
        val grant_type: String,
        val redirect_uri: String,
        val code_verifier: String
)