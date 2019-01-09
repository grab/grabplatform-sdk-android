/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk.models

import java.util.Date

internal const val EMPTY_STRING_CONST = ""
class LoginSession {
    /****************************************************************
    // properties that partner app will set
     ****************************************************************/
    var clientId: String = EMPTY_STRING_CONST
    var redirectUri: String = EMPTY_STRING_CONST
    var serviceDiscoveryUrl: String = EMPTY_STRING_CONST
    var scope: String = EMPTY_STRING_CONST
    // Used by app for one time transactions scenario - base64 encoded jwt
    var request: String? = EMPTY_STRING_CONST
    var loginHint: String? = EMPTY_STRING_CONST
    // The OpenID Connect ACR optional parameter to the authorize endpoint will be utilized to pass in
    // service id info and device ID
    var acrValues: String? = EMPTY_STRING_CONST

    /****************************************************************
    // parameters to be set by GrabId Partner SDK
     ****************************************************************/
    internal var codeInternal: String = EMPTY_STRING_CONST
    val code get() = codeInternal

    internal var codeVerifierInternal: String = EMPTY_STRING_CONST
    val codeVerifier get() = codeVerifierInternal

    internal var accessTokenExpiresAtInternal: Date? = null
    val accessTokenExpiresAt get() = accessTokenExpiresAtInternal

    internal var stateInternal: String = EMPTY_STRING_CONST
    val state get() = stateInternal

    internal var tokenTypeInternal: String = EMPTY_STRING_CONST
    val tokenType get() = tokenTypeInternal

    internal var nonceInternal: String = EMPTY_STRING_CONST
    val nonce get() = nonceInternal

    // all tokens
    internal var accessTokenInternal: String = EMPTY_STRING_CONST
    val accessToken get() = accessTokenInternal

    internal var idTokenInternal: String = EMPTY_STRING_CONST
    val idToken get() = idTokenInternal

    internal var refreshTokenInternal: String = EMPTY_STRING_CONST
    val refreshToken get() = refreshTokenInternal

    /****************************************************************
    // internal to GrabId Partner SDK
     ****************************************************************/
    internal var codeChallenge: String = EMPTY_STRING_CONST
    internal var isDebug: Boolean = false

    /****************************************************************
    // endpoints
     ****************************************************************/
    internal var authorizationEndpoint: String? = null
    internal var tokenEndpoint: String? = null
    internal var idTokenVerificationEndpoint: String? = null
}