/*
 * Copyright (c) 2012-2019 Grab Taxi Holdings PTE LTD (GRAB), All Rights Reserved. NOTICE: All information contained herein is, and remains the property of GRAB. The intellectual and technical concepts contained herein are confidential, proprietary and controlled by GRAB and may be covered by patents, patents in process, and are protected by trade secret or copyright law.
 * You are strictly forbidden to copy, download, store (in any medium), transmit, disseminate, adapt or change this material in any way unless prior written permission is obtained from GRAB. Access to the source code contained herein is hereby forbidden to anyone except current GRAB employees or contractors with binding Confidentiality and Non-disclosure agreements explicitly covering such access.
 *
 * The copyright notice above does not evidence any actual or intended publication or disclosure of this source code, which includes information that is confidential and/or proprietary, and is a trade secret, of GRAB.
 * ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC PERFORMANCE, OR PUBLIC DISPLAY OF OR THROUGH USE OF THIS SOURCE CODE WITHOUT THE EXPRESS WRITTEN CONSENT OF GRAB IS STRICTLY PROHIBITED, AND IN VIOLATION OF APPLICABLE LAWS AND INTERNATIONAL TREATIES. THE RECEIPT OR POSSESSION OF THIS SOURCE CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY ANY RIGHTS TO REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR SELL ANYTHING THAT IT MAY DESCRIBE, IN WHOLE OR IN PART.
 */

package com.grab.partner.sdk.models

enum class GrabIdPartnerErrorCode {
    unknown,                        // This error is unexpected or cannot be exposed.
    network,                        // There is an issue with network connectivity.
    sdkNotInitialized,              // SDK is not initialized
    invalidClientId,                // Invalid client id
    errorInLoginSessionObject,      // Error in loginSession object
    errorRetrievingObjectFromSharedPref, // Error retrieving object from shared preferences
    invalidRedirectURI,             // Invalid redirect uri
    invalidDiscoveryEndpoint,       // Invalid discovery endpoint
    errorInDiscoveryEndpoint,       // Error while fetching discovery endpoint
    errorInExchangeTokenEndpoint,   // Error while fetching discovery endpoint
    missingAccessToken,             // Access token is missing
    missingIdToken,                 // Id token is missing
    missingAccessTokenExpiry,       // Access token expiry is missing
    errorInGetIdTokenInfo,          // Error in getIdTokenInfo endpoint
    invalidAuthorizationCode,       // Invalid authorization code
    invalidAuthorizationUrl,        // The authorize end point is invalid
    invalidPartnerId,               // Partner id is not set in AndroidManifest.
    invalidPartnerScope,            // Partner scope is not set in AndroidManifest
    unAuthorized,                   // Partner application is unauthorized.
    serviceUnavailable,             // The service was not available.
    internalServerError,            // There was an issue with Grab server.
    invalidAccessToken,             // The access token is invalid.
    invalidIdToken,                 // The id token is invalid.
    invalidCode,                    // The code is invalid
    stateMismatch,                  // State received from the redirect url doesn't match with loginSession
    invalidNonce,                   // The nonce is invalid
    invalidResponse,                // Unexpected response from GrabId service
    errorInLogout                   // Error in logout operation
}

class GrabIdPartnerError(grabIdPartnerErrorDomain: GrabIdPartnerErrorDomain, code: GrabIdPartnerErrorCode, localizeMessage: String?, serviceError: Throwable?) {
    var grabIdPartnerErrorDomain: GrabIdPartnerErrorDomain = GrabIdPartnerErrorDomain.UNKNOWN
    var code: GrabIdPartnerErrorCode = GrabIdPartnerErrorCode.unknown // source of the error
    var localizeMessage: String? = null              // localized error message
    var serviceError: Throwable? = null            // system error

    init {
        this.code = code
        this.localizeMessage = localizeMessage
        this.serviceError = serviceError
        this.grabIdPartnerErrorDomain = grabIdPartnerErrorDomain
    }
}

enum class GrabIdPartnerErrorDomain {
    LOADLOGINSESSION,
    LOGIN,
    SERVICEDISCOVERY,
    EXCHANGETOKEN,
    GETIDTOKENINFO,
    LOGOUT,
    PROTECTED_RESOURCES,
    UNKNOWN
}