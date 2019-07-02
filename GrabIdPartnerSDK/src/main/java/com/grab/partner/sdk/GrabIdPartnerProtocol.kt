/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk

import android.content.Context
import com.grab.partner.sdk.models.IdTokenInfo
import com.grab.partner.sdk.models.LoginSession

interface GrabIdPartnerProtocol {
    fun initialize(context: Context)
    fun loadLoginSession(callback: LoginSessionCallback)
    fun login(loginSession: LoginSession, context: Context, callback: LoginCallback)
    fun login(loginSession: LoginSession, context: Context, callback: LoginCallbackV2)
    fun exchangeToken(loginSession: LoginSession, redirectUrl: String, callback: ExchangeTokenCallback)
    fun getIdTokenInfo(loginSession: LoginSession, callback: GetIdTokenInfoCallback)
    fun logout(loginSession: LoginSession, callback: LogoutCallback)
    fun teardown()
    fun isValidAccessToken(loginSession: LoginSession): Boolean
    fun isValidIdToken(idTokenInfo: IdTokenInfo): Boolean
}