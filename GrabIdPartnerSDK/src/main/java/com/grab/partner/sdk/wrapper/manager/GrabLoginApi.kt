/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */
package com.grab.partner.sdk.wrapper.manager

import android.content.Context
import com.grab.partner.sdk.models.LoginSession

interface GrabLoginApi {
    fun doLogin(context: Context, state: String, builder: GrabSdkManager.Builder)
    fun exchangeToken(loginSession: LoginSession, redirectUrl: String, builder: GrabSdkManager.Builder)
}