/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk.sampleapp.api

import android.content.Context
import com.grab.partner.sdk.sampleapp.models.UserInfoAPIResponse
import com.grab.partner.sdk.sampleapp.utils.PROTECTED_RESOURCE_ENDPOINT_ATTRIBUTE
import com.grab.partner.sdk.sampleapp.utils.Utility
import io.reactivex.Observable
import javax.inject.Inject

class GrabRepositoryImpl @Inject constructor(private val context: Context,
        private val grabAPI: GrabAPI) : GrabRepository {
    override fun getProtectedAPIResponse(token: String): Observable<UserInfoAPIResponse> {
        return grabAPI.getProtectedAPIResponse(Utility.readMetadata(context, PROTECTED_RESOURCE_ENDPOINT_ATTRIBUTE)?: "", token)
    }
}
