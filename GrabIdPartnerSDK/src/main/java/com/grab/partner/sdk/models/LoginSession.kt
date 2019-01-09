/*
 * Copyright (c) 2012-2019 Grab Taxi Holdings PTE LTD (GRAB), All Rights Reserved. NOTICE: All information contained herein is, and remains the property of GRAB. The intellectual and technical concepts contained herein are confidential, proprietary and controlled by GRAB and may be covered by patents, patents in process, and are protected by trade secret or copyright law.
 * You are strictly forbidden to copy, download, store (in any medium), transmit, disseminate, adapt or change this material in any way unless prior written permission is obtained from GRAB. Access to the source code contained herein is hereby forbidden to anyone except current GRAB employees or contractors with binding Confidentiality and Non-disclosure agreements explicitly covering such access.
 *
 * The copyright notice above does not evidence any actual or intended publication or disclosure of this source code, which includes information that is confidential and/or proprietary, and is a trade secret, of GRAB.
 * ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC PERFORMANCE, OR PUBLIC DISPLAY OF OR THROUGH USE OF THIS SOURCE CODE WITHOUT THE EXPRESS WRITTEN CONSENT OF GRAB IS STRICTLY PROHIBITED, AND IN VIOLATION OF APPLICABLE LAWS AND INTERNATIONAL TREATIES. THE RECEIPT OR POSSESSION OF THIS SOURCE CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY ANY RIGHTS TO REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR SELL ANYTHING THAT IT MAY DESCRIBE, IN WHOLE OR IN PART.
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

    internal var codeVerifier: String = EMPTY_STRING_CONST

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