/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.customtabs.CustomTabsClient
import android.support.customtabs.CustomTabsIntent
import android.support.customtabs.CustomTabsServiceConnection
import android.support.customtabs.CustomTabsSession
import com.grab.partner.sdk.GrabIdPartner.Companion.CODE_CHALLENGE_METHOD
import com.grab.partner.sdk.GrabIdPartner.Companion.RESPONSE_TYPE
import com.grab.partner.sdk.LoginCallback
import com.grab.partner.sdk.models.LoginSession

internal interface IChromeCustomTab {
    /**
     * Open Chrome Custom Tab to start the OAuth 2.0 process and then return back to WebLogin activity through the deep link redirect URL
     */
    fun openChromeCustomTab(context: Context, loginSession: LoginSession, callback: LoginCallback)

    /**
     * Speed up Chrome tab
     */
    fun speedUpChromeTabs()
}

internal class ChromeCustomTab : IChromeCustomTab {

    // Chrome Custom Tab variables
    private var mClient: CustomTabsClient? = null
    private var mCustomTabsSession: CustomTabsSession? = null
    private var mCustomTabsServiceConnection: CustomTabsServiceConnection? = null
    private lateinit var customTabsIntent: CustomTabsIntent
    private val CUSTOM_TAB_PACKAGE_NAME = "com.android.chrome"
    /**
     * Open Chrome Custom Tab to start the OAuth 2.0 process and then return back to WebLogin activity through the deep link redirect URL
     */
    override fun openChromeCustomTab(context: Context, loginSession: LoginSession, callback: LoginCallback) {
        val builder = Uri.parse(loginSession.authorizationEndpoint).buildUpon()
        builder.appendQueryParameter("client_id", loginSession.clientId)
                .appendQueryParameter("code_challenge", loginSession.codeChallenge)
                .appendQueryParameter("code_challenge_method", CODE_CHALLENGE_METHOD)
                .appendQueryParameter("nonce", loginSession.nonce)
                .appendQueryParameter("redirect_uri", loginSession.redirectUri)
                .appendQueryParameter("response_type", RESPONSE_TYPE)
                .appendQueryParameter("state", loginSession.state)
                .appendQueryParameter("scope", loginSession.scope)

        // add optional user provided parameters if those exists
        if (!loginSession.acrValues.isNullOrBlank()) {
            builder.appendQueryParameter("acr_values", loginSession.acrValues)
        }
        if (!loginSession.request.isNullOrBlank()) {
            builder.appendQueryParameter("request", loginSession.request)
        }
        if (!loginSession.loginHint.isNullOrBlank()) {
            builder.appendQueryParameter("id_token_hint", loginSession.loginHint)
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            CustomTabsClient.bindCustomTabsService(context, CUSTOM_TAB_PACKAGE_NAME, mCustomTabsServiceConnection)

            customTabsIntent = CustomTabsIntent.Builder(mCustomTabsSession)
                    .setShowTitle(true)
                    .build()

            customTabsIntent.intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            customTabsIntent.intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            customTabsIntent.launchUrl(context, Uri.parse(builder.build().toString()))
        }

        callback.onSuccess()
    }

    /**
     * Speed up Chrome tab
     */
    override fun speedUpChromeTabs() {
        mCustomTabsServiceConnection = object : CustomTabsServiceConnection() {
            override fun onCustomTabsServiceConnected(componentName: ComponentName, customTabsClient: CustomTabsClient) {
                //Pre-warming
                mClient = customTabsClient
                mClient?.warmup(0L)
                mCustomTabsSession = mClient?.newSession(null)

            }

            override fun onServiceDisconnected(name: ComponentName) {
                mClient = null
            }
        }
    }
}