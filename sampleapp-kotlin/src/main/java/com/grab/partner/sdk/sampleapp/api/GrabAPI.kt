/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk.sampleapp.api

import com.grab.partner.sdk.sampleapp.models.UserInfoAPIResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Url

interface GrabAPI {
    @GET
    fun getProtectedAPIResponse(@Url url: String,
                                @Header("Authorization") token: String
    ): io.reactivex.Observable<UserInfoAPIResponse>
}