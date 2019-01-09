/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
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