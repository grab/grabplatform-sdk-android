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
import com.grab.partner.sdk.R
import com.grab.partner.sdk.models.GrabIdPartnerError
import com.grab.partner.sdk.models.GrabIdPartnerErrorCode
import com.grab.partner.sdk.models.GrabIdPartnerErrorDomain
import com.grab.partner.sdk.utils.IUtility
import com.grab.partner.sdk.wrapper.di.DaggerWrapperComponent
import com.grab.partner.sdk.wrapper.di.WrapperComponent
import com.grab.partner.sdk.wrapper.manager.GrabSdkManagerImpl
import javax.inject.Inject

/**
 * This activity will help to remove the 'Chrome Custom Tab' from the back stack, if client app using the SDK wrapper flow and SDK is launching the Chrome tab for this OAuth transaction.
 *
 * Scenario1:
 * Here is the what will happen during a normal OAuth flow -
 * 1. ChromeManagerActivity launchMode is singleTask and it will be launched before SDK launches 'Chrome Custom Tab'.
 * 2. After successful OAuth flow DeeplinkActivity will receive the deeplink, and DeeplinkActivity will relaunch ChromeManagerActivity.
 * 3. onNewIntent of ChromeManagerActivity will be invoked. As the launchMode of ChromeManagerActivity is singleTask so Android will remove whatever activities we have on top of this activity,
 * in this case 'Chrome Custom Tab' will be removedfrom the back stack and ChromeManagerActivity will be on the top of the stack.Then we will finish this activity inside onNewIntent so user
 * will go back to the client app activity from where the login flow originally started.
 *
 * Scenario2:
 * Here is the what will happen if user cancels the OAuth flow by closing the 'Chrome Custom Tab' -
 * 1. In this case DeeplinkActivity will not be invoked as there is no redirect coming from GrabID backend.
 * 2. ChromeManagerActivity > onResume will be invoked as this is the top activity on the back stack.
 * 3. We will return error from onResume and will finish this activity so the caller will receive the error via callback and decide what to do.
 */
// to keep track of whether ChromeManagerActivity launched during this OAuth transaction or not. ChromeManagerActivity will not be launched during Native app login flow or if SDK launches the appstore
var HAVE_LAUNCHED_CHROME_TAB_MANAGER_ACTIVITY: Boolean = false

class ChromeManagerActivity : AppCompatActivity() {
    private var callback: LoginCallback? = null

    @Inject
    lateinit var utility: IUtility
    lateinit var component: WrapperComponent

    override fun onCreate(savedInstanceState: Bundle?) {
        setupDI()
        super.onCreate(savedInstanceState)
        val repo = GrabIdPartner.mainComponent.getGrabIdPartnerRepo()
        val uri = repo.getUri()
        callback = repo.getLoginCallback()
        HAVE_LAUNCHED_CHROME_TAB_MANAGER_ACTIVITY = false
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
                finish()
            }
        }
    }

    private fun setupDI() {
        component = DaggerWrapperComponent.builder().build()
        component.inject(this)
    }

    override fun onResume() {
        super.onResume()
        if (HAVE_LAUNCHED_CHROME_TAB_MANAGER_ACTIVITY) {
            // if we're here meaning user actually didn't complete the OAuth flow
            callback?.onError(
                GrabIdPartnerError(
                    GrabIdPartnerErrorDomain.LOGIN,
                    GrabIdPartnerErrorCode.loginCancelledByUser,
                    utility.readResourceString(this, R.string.ERROR_USER_CANCELLED_LOGIN_FLOW),
                    null
                )
            )
            finish()
        } else {
            HAVE_LAUNCHED_CHROME_TAB_MANAGER_ACTIVITY = true
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        // this is to avoid memory leak
        GrabIdPartner.mainComponent.getChromeTabLauncher().unbindChromeServiceConnection(this)
    }
}