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
import javax.inject.Inject

internal class GrabAuthRepositoryImpl @Inject constructor(private val grabSdkApi: GrabSdkApi) : GrabAuthRepository {
    override fun callDiscovery(discoveryEndpoint: String): Observable<DiscoveryResponse> {
        return grabSdkApi.fetchDiscovery(discoveryEndpoint)
    }

    override fun getToken(tokenEndpoint: String, tokenRequest: TokenRequest): Observable<TokenAPIResponse> {
        return grabSdkApi.getToken(tokenEndpoint, tokenRequest)
    }

    override fun getIdTokenInfo(idTokenEndpoint: String, client_id: String, id_token: String, nonce: String): Observable<IdTokenInfo> {
        return grabSdkApi.getIdTokenInfo(idTokenEndpoint, client_id, id_token, nonce)
    }
}