/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk.utils

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession
import com.grab.partner.sdk.GrabIdPartner.Companion.CODE_CHALLENGE_METHOD
import com.grab.partner.sdk.GrabIdPartner.Companion.RESPONSE_TYPE
import com.grab.partner.sdk.LoginCallback
import com.grab.partner.sdk.models.GrabIdPartnerError
import com.grab.partner.sdk.models.GrabIdPartnerErrorCode
import com.grab.partner.sdk.models.GrabIdPartnerErrorDomain
import com.grab.partner.sdk.models.LoginSession

internal interface LaunchAppForAuthorization {
    /**
     * Launch OAuth flow Using native Grab App if available or fall back to either launching playstore
     * if link is available or falling back to weblogin via Chrome Custom Tab
     */
    fun launchOAuthFlow(context: Context, loginSession: LoginSession, callback: LoginCallback, shouldLaunchNativeApp: Boolean = false)

    /**
     * Speed up Chrome tab
     */
    fun speedUpChromeTabs()
}


internal class LaunchAppForAuthorizationImpl : LaunchAppForAuthorization {

    // Chrome Custom Tab variables
    private var mClient: CustomTabsClient? = null
    private var mCustomTabsSession: CustomTabsSession? = null
    private lateinit var mCustomTabsServiceConnection: CustomTabsServiceConnection
    private lateinit var customTabsIntent: CustomTabsIntent
    private val CUSTOM_TAB_PACKAGE_NAME = "com.android.chrome"
    internal var intentProvider: IntentProvider = IntentProviderImpl()

    /**
     *  Launch OAuth flow using native Grab app then return back to client activity through the deep link redirect URL, if Native app is
     *  not available we will fallback to either Chrome Custom Tab or launch a link to the playstore
     *  we will only launch into the playstore if a link is available otherwise we will fallback
     *  to chrome custom tabs
     */
    override fun launchOAuthFlow(context: Context, loginSession: LoginSession, callback: LoginCallback, shouldLaunchNativeApp: Boolean) {
        val uri = setupUri(loginSession, shouldLaunchNativeApp)

        if (shouldLaunchNativeApp) {
            // launch the deep link
            try {
               return launchPartnerLogin(context, uri, loginSession, callback)
            } catch (ex: Exception) {
                //todo possibly add qem
            }
        }

        //if playstore link is available force to playstore
        if (loginSession.playstoreLink.isNotEmpty()) {
            launchPlaystore(context, loginSession, callback)
        } else {
            try {
                // launch chrome custom tab
                launchChromeCustomTab(context, uri, callback)
            } catch (ex: Exception) {
                //can't do anything here, so sending the onError callback
                callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.LAUNCHOAUTHFLOW, GrabIdPartnerErrorCode.errorLaunchingChromeCustomTab, ex.localizedMessage, null))
            }
        }
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

    private fun setupUri(loginSession: LoginSession, isNativeAppAvailable: Boolean): Uri {
        val builder = if (isNativeAppAvailable) {
            Uri.parse(loginSession.deeplinkUri)
                    .buildUpon()
                    .appendQueryParameter("auth_endpoint", loginSession.authorizationEndpoint)
        } else {
            Uri.parse(loginSession.authorizationEndpoint).buildUpon()
                    .apply {
                        if (loginSession.loginHint?.isNotBlank() == true) {
                            this.appendQueryParameter("login_hint", loginSession.loginHint)
                        }
                        if (loginSession.idTokenHint?.isNotBlank() == true) {
                            this.appendQueryParameter("id_token_hint", loginSession.idTokenHint)
                        }
                        if (loginSession.prompt?.isNotBlank() == true) {
                            this.appendQueryParameter("prompt", loginSession.prompt)
                        }
                    }
        }

        builder.appendQueryParameter("client_id", loginSession.clientId)
                .appendQueryParameter("code_challenge", loginSession.codeChallenge)
                .appendQueryParameter("code_challenge_method", CODE_CHALLENGE_METHOD)
                .appendQueryParameter("nonce", loginSession.nonce)
                .appendQueryParameter("redirect_uri", loginSession.redirectUri)
                .appendQueryParameter("response_type", RESPONSE_TYPE)
                .appendQueryParameter("state", loginSession.state)
                .appendQueryParameter("scope", loginSession.scope)

        // add optional user provided parameters if exists
        if (loginSession.acrValues?.isNotBlank() == true) {
            builder.appendQueryParameter("acr_values", loginSession.acrValues)
        }
        if (loginSession.request?.isNotBlank() == true) {
            builder.appendQueryParameter("request", loginSession.request)
        }
        return builder.build()
    }

    private fun launchChromeCustomTab(context: Context, uri: Uri, callback: LoginCallback) {
        // launch chrome custom tab
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            CustomTabsClient.bindCustomTabsService(context, CUSTOM_TAB_PACKAGE_NAME, mCustomTabsServiceConnection)

            customTabsIntent = CustomTabsIntent.Builder(mCustomTabsSession)
                    .setShowTitle(true)
                    .build()

            customTabsIntent.intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            // if user is using the old login api and sending the application context then we have to add this FLAG_ACTIVITY_NEW_TASK flag
            if (context is Application) {
                customTabsIntent.intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            customTabsIntent.launchUrl(context, uri)
        }
        callback.onSuccess()
    }

    private fun launchPartnerLogin(context: Context, uri: Uri, loginSession: LoginSession, callback: LoginCallback) {
        val intent = intentProvider.provideIntent(Intent.ACTION_VIEW)
        intent.data = uri
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
            callback.onSuccess()
        } else {
            // if for any reason we fail to resolve the activity then launch the Chrome Custom Tab flow
            launchOAuthFlow(context, loginSession, callback)
        }
    }

    /**
     * Will launch intent into the playstore link if it is available and there is a package that
     * can launch the given module.
     *
     * will call [LoginCallback] onError with correct [GrabIdPartnerErrorCode] if intent is launched
     * by system or fails to launch so that consumer will not wait for success deeplink
     */
    private fun launchPlaystore(context: Context, loginSession: LoginSession, callback: LoginCallback) {
        val intent = intentProvider.provideIntent(Intent.ACTION_VIEW)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        var thrown: Throwable? = null
        var code: GrabIdPartnerErrorCode = GrabIdPartnerErrorCode.failedTolaunchAppStoreLink
        //if success we will override the above value

        if (intent.resolveActivity(context.packageManager) != null) {
            try {
                intent.data = Uri.parse(loginSession.playstoreLink)
                context.startActivity(intent)
                code = GrabIdPartnerErrorCode.launchAppStoreLink
            } catch (throwable: Throwable) {
                //possible log in future
                thrown = throwable
            }
        }
        //log error as technically this is not the correct way of handling and will never get redirect
        callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.LAUNCHOAUTHFLOW,
                code,
                loginSession.playstoreLink,
                null))
    }
}