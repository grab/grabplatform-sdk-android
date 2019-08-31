/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */
package com.grab.partner.sdk.models

internal data class ClientPublicInfo(
        val custom_protocols: List<String>,
        val home_page_url: String,
        val logo_url: String,
        val privacy_policy_url: String,
        val product_name: String,
        val terms_of_service_url: String
)

data class ProtocolInfo(
        val package_adr: String,
        val minversion_adr: String,
        val protocol_adr: String
)