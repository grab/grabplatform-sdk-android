/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk.models

import com.google.gson.annotations.SerializedName
import java.util.Date

class IdTokenInfo {
    @SerializedName("aud")
    internal var audienceInternal: String? = ""
    val audience get() = audienceInternal

    @SerializedName("svc")
    internal var serviceInternal: String? = ""
    val service get() = serviceInternal

    @SerializedName("nbf")
    internal var notValidBeforeInternal: Date? = null
    val notValidBefore get() = notValidBeforeInternal

    @SerializedName("exp")
    internal var expirationInternal: Date? = null
    val expiration get() = expirationInternal

    @SerializedName("iat")
    internal var issueDateInternal: Date? = null
    val issueDate get() = issueDateInternal

    @SerializedName("iss")
    internal var issuerInternal: String? = ""
    val issuer get() = issuerInternal

    @SerializedName("jti")
    internal var tokenIdInternal: String? = ""
    val tokenId get() = tokenIdInternal

    @SerializedName("pid")
    internal var partnerIdInternal: String? = ""
    val partnerId get() = partnerIdInternal

    @SerializedName("sub")
    internal var partnerUserIdInternal: String? = ""
    val partnerUserId get() = partnerUserIdInternal

    @SerializedName("nonce")
    internal var nonceInternal: String? = ""
    val nonce get() = nonceInternal
}