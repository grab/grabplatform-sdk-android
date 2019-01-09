/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk.models

internal data class DiscoveryResponse(
        val authorization_endpoint: String?="",
        val token_endpoint: String?="",
        val id_token_verification_endpoint: String?=""
)