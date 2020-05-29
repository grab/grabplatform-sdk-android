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
import com.grab.partner.sdk.LoginCallback

internal const val CUSTOM_TAB_PACKAGE_NAME = "com.android.chrome"

internal interface ChromeTabLauncher {
    fun launchChromeTab(context: Context, uri: Uri, callback: LoginCallback?)
}

internal class ChromeTabLauncherImpl : ChromeTabLauncher {
    // Chrome Custom Tab variables
    private var mClient: CustomTabsClient? = null
    private var mCustomTabsSession: CustomTabsSession? = null
    private var mCustomTabsServiceConnection: CustomTabsServiceConnection
    private lateinit var customTabsIntent: CustomTabsIntent

    init {
        mCustomTabsServiceConnection = object : CustomTabsServiceConnection() {
            override fun onCustomTabsServiceConnected(
                componentName: ComponentName,
                customTabsClient: CustomTabsClient
            ) {
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

    override fun launchChromeTab(context: Context, uri: Uri, callback: LoginCallback?) {
        // launch chrome custom tab
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            CustomTabsClient.bindCustomTabsService(
                context,
                CUSTOM_TAB_PACKAGE_NAME,
                mCustomTabsServiceConnection
            )

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
        callback?.onSuccess()
    }
}