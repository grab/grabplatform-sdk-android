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
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

internal interface GrabSdkApi {
    // future work: convert these to Single
    @GET
    fun fetchDiscovery(@Url url: String): Observable<DiscoveryResponse>

    @GET
    fun fetchClientInfo(@Url url: String): Observable<ClientPublicInfo>

    @POST
    fun getToken(@Url url : String,
            @Body request: TokenRequest
    ): Observable<TokenAPIResponse>

    @GET
    fun getIdTokenInfo(@Url url: String,
                      @Query("client_id") client_id: String,
                      @Query("id_token") id_token: String,
                      @Query("nonce") nonce: String
    ): Observable<IdTokenInfo>
}