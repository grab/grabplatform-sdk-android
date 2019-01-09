/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk.api

import com.grab.partner.sdk.models.DiscoveryResponse
import com.grab.partner.sdk.models.IdTokenInfo
import com.grab.partner.sdk.models.TokenAPIResponse
import com.grab.partner.sdk.models.TokenRequest
import io.reactivex.Observable

internal class StubGrabSdkApi: GrabSdkApi {
    private var discoveryResponse: DiscoveryResponse? = null
    private var tokenAPIResponse: TokenAPIResponse? = null
    private var idTokenInfo: IdTokenInfo? = null

    override fun fetchDiscovery(url: String): Observable<DiscoveryResponse> {
        return Observable.just(this.discoveryResponse)
    }

    fun setFetchDiscoveryAPIResponse(discoveryResponse: DiscoveryResponse) {
        this.discoveryResponse = discoveryResponse
    }

    override fun getToken(url: String, request: TokenRequest): Observable<TokenAPIResponse> {
        return Observable.just(tokenAPIResponse)
    }

    fun setGetTokenAPIResponse(tokenAPIResponse: TokenAPIResponse) {
        this.tokenAPIResponse = tokenAPIResponse
    }

    override fun getIdTokenInfo(url: String, client_id: String, id_token: String, nonce: String): Observable<IdTokenInfo> {
        return Observable.just(idTokenInfo)
    }

    fun setGetIdTokenInfoAPIResponse(idTokenInfo: IdTokenInfo) {
        this.idTokenInfo = idTokenInfo
    }
}