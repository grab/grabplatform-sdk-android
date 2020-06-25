/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk.sampleappkotlin.wrapper.api

import com.grab.partner.sdk.sampleappkotlin.wrapper.models.UserInfoAPIResponse
import io.reactivex.Observable

interface GrabRepository {
    fun getProtectedAPIResponse(token: String): Observable<UserInfoAPIResponse>
}
