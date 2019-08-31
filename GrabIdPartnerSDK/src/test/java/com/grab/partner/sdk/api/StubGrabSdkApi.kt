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

internal class StubGrabSdkApi : GrabSdkApi {
    private var discoveryResponse: DiscoveryResponse? = null
    private var tokenAPIResponse: TokenAPIResponse? = null
    private var idTokenInfo: IdTokenInfo? = null
    private var clientPublicInfo: ClientPublicInfo? = null

    override fun fetchDiscovery(url: String): Observable<DiscoveryResponse> {
        return Observable.just(this.discoveryResponse)
    }

    fun setFetchDiscoveryAPIResponse(discoveryResponse: DiscoveryResponse) {
        this.discoveryResponse = discoveryResponse
    }

    override fun fetchClientInfo(url: String): Observable<ClientPublicInfo> {
        return Observable.just(this.clientPublicInfo)
    }

    fun setFetchClientInfo(clientPublicInfo: ClientPublicInfo) {
        this.clientPublicInfo = clientPublicInfo
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