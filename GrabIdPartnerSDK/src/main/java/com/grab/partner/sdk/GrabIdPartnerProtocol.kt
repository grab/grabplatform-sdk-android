/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk

import android.app.Activity
import android.content.Context
import com.grab.partner.sdk.models.IdTokenInfo
import com.grab.partner.sdk.models.LoginSession

interface GrabIdPartnerProtocol {
    @Deprecated(message = "Usages of this API is deprecated", replaceWith = ReplaceWith("com.grab.partner.sdk.GrabIdPartnerProtocol.initialize(android.content.Context, com.grab.partner.sdk.InitializeCallback)"))
    fun initialize(context: Context)

    /**
     * Initialize the SDK using application context
     */
    fun initialize(context: Context, callback: InitializeCallback? = null)

    /**
     * Generate loginSession object. This api will construct the loginSession using the parameters supplied in the AndroidManifest.xml file.
     * Note: com.grab.partner.sdk.GrabIdPartnerProtocol.initialize(android.content.Context, com.grab.partner.sdk.InitializeCallback) should be invoked first.
     */
    fun loadLoginSession(callback: LoginSessionCallback)

    /**
     * Generate loginSession object. This api will construct the loginSession object using the parameters supplied.
     * Note: com.grab.partner.sdk.GrabIdPartnerProtocol.initialize(android.content.Context, com.grab.partner.sdk.InitializeCallback) should be invoked first.
     */
    fun loadLoginSession(state: String, clientId: String, redirectUri: String, serviceDiscoveryUrl: String,
                         scope: String, acrValues: String?, request: String?, loginHint: String?, idTokenHint: String?, callback: LoginSessionCallback, prompt: String? = "")

    @Deprecated(message = "Usages of this API is deprecated", replaceWith = ReplaceWith("com.grab.partner.sdk.GrabIdPartnerProtocol.loginV2"))
    fun login(loginSession: LoginSession, context: Context, callback: LoginCallback)

    @Deprecated(message = "Usages of this API is deprecated", replaceWith = ReplaceWith("com.grab.partner.sdk.GrabIdPartnerProtocol.loginV2"))
    fun login(loginSession: LoginSession, context: Context, callback: LoginCallbackV2)

    /**
     * Start the login process using GrabIdPartnerSDK.
     */
    fun loginV2(loginSession: LoginSession, activity: Activity, callback: LoginCallbackV2)

    /**
     * API to get token using the code received as redirect parameter.
     */
    fun exchangeToken(loginSession: LoginSession, redirectUrl: String, callback: ExchangeTokenCallback)

    /**
     * Validate exchangeToken token is valid using token info endpoint.
     */
    fun getIdTokenInfo(loginSession: LoginSession, callback: GetIdTokenInfoCallback)

    /**
     * Clear all login information from shared preference and Android keystore.
     */
    fun logout(loginSession: LoginSession, callback: LogoutCallback)

    /**
     * Unsubscribe all observable subscription and to clear other objects created during the login process.
     */
    fun teardown()

    /**
     * Validate if access token is still valid.
     */
    fun isValidAccessToken(loginSession: LoginSession): Boolean

    /**
     * Validate whether id token is still valid
     */
    fun isValidIdToken(idTokenInfo: IdTokenInfo): Boolean
}