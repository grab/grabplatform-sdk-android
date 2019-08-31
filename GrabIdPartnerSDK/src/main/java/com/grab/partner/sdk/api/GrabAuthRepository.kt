/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk.api

import com.grab.partner.sdk.models.*
import io.reactivex.Observable

internal interface GrabAuthRepository {
    fun callDiscovery(discoveryEndpoint: String): Observable<DiscoveryResponse>
    fun fetchClientPublicInfo(clientPublicEndpoint: String): Observable<ClientPublicInfo>
    fun getToken(tokenEndpoint: String, tokenRequest: TokenRequest): Observable<TokenAPIResponse>
    fun getIdTokenInfo(idTokenEndpoint: String, client_id: String, id_token: String, nonce: String): Observable<IdTokenInfo>
}