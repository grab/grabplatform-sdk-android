/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */
package com.grab.partner.sdk.wrapper.chrometabmanager

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.grab.partner.sdk.GrabIdPartner
import com.grab.partner.sdk.LoginCallback
import com.grab.partner.sdk.models.GrabIdPartnerError
import com.grab.partner.sdk.models.GrabIdPartnerErrorCode
import com.grab.partner.sdk.models.GrabIdPartnerErrorDomain

/**
 * This activity will help to remove the 'Chrome Custom Tab' from the back stack, if client app using the SDK wrapper flow and SDK is launching the Chrome tab for this OAuth transaction.
 * ChromeTabManagerActivity launchMode is singleTask and it will be launched before SDK launches 'Chrome Custom Tab'. After successful OAuth flow DeeplinkActivity will receive the deeplink,
 * and DeeplinkActivity will relaunch this activity, and as the launchMode is singleTask so Android will remove whatever activities we have on top of this activity, in this case 'Chrome Custom Tab'
 * will be removed from the back stack and ChromeTabManagerActivity will be on the top of the stack. Then in the onNewIntent of this activity we will finish this activity so user will see
 * the client app activity from where the login flow started.
 */
// to keep track of whether ChromeManagerActivity launched during this OAuth transaction or not. ChromeManagerActivity will not be launched during Native app login flow or if SDK launches the appstore
var HAVE_LAUNCHED_CHROME_TAB_MANAGER_ACTIVITY: Boolean = false

class ChromeManagerActivity : AppCompatActivity() {
    private var callback: LoginCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        HAVE_LAUNCHED_CHROME_TAB_MANAGER_ACTIVITY = true
        val repo = GrabIdPartner.mainComponent.getGrabIdPartnerRepo()
        val uri = repo.getUri()
        callback = repo.getLoginCallback()
        uri?.let {
            try {
                // launch chrome custom tab
                GrabIdPartner.mainComponent.getChromeTabLauncher()
                    .launchChromeTab(this, it, callback)
            } catch (ex: Exception) {
                //can't do anything here, so sending the onError callback
                callback?.onError(
                    GrabIdPartnerError(
                        GrabIdPartnerErrorDomain.LAUNCHOAUTHFLOW,
                        GrabIdPartnerErrorCode.errorLaunchingChromeCustomTab,
                        ex.localizedMessage,
                        null
                    )
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        finish()
    }
}