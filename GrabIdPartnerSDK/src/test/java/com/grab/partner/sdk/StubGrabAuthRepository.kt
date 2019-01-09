/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk

import com.grab.partner.sdk.api.GrabAuthRepository
import com.grab.partner.sdk.models.DiscoveryResponse
import com.grab.partner.sdk.models.IdTokenInfo
import com.grab.partner.sdk.models.TokenAPIResponse
import com.grab.partner.sdk.models.TokenRequest
import io.reactivex.Observable

internal class StubGrabAuthRepository : GrabAuthRepository {
    private lateinit var discoveryResponse: Observable<DiscoveryResponse>
    private lateinit var tokenAPIResponse: Observable<TokenAPIResponse>
    private lateinit var idTokenInfo: Observable<IdTokenInfo>

    override fun callDiscovery(discoveryEndpoint: String): Observable<DiscoveryResponse> {
        return discoveryResponse
    }

    fun setCallDiscovery(discoveryResponse: Observable<DiscoveryResponse>) {
        this.discoveryResponse = discoveryResponse
    }

    override fun getToken(tokenEndpoint: String, tokenRequest: TokenRequest): Observable<TokenAPIResponse> {
        return this.tokenAPIResponse
    }

    fun setGetToken(tokenAPIResponse: Observable<TokenAPIResponse>) {
        this.tokenAPIResponse = tokenAPIResponse
    }

    override fun getIdTokenInfo(idTokenEndpoint: String, client_id: String, id_token: String, nonce: String): Observable<IdTokenInfo> {
        return idTokenInfo
    }

    fun setGetIdTokenInfo(idTokenInfo: Observable<IdTokenInfo>) {
        this.idTokenInfo = idTokenInfo
    }
}