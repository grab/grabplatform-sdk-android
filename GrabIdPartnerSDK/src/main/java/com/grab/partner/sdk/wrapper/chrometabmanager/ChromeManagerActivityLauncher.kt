/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */
package com.grab.partner.sdk.wrapper.chrometabmanager

import android.content.Context
import android.content.Intent

internal interface ChromeManagerActivityLauncher {
    fun launchChromeManagerActivity(context: Context)
}

internal class ChromeTabManagerLauncherImpl : ChromeManagerActivityLauncher {
    override fun launchChromeManagerActivity(context: Context) {
        context.startActivity(Intent(context, ChromeManagerActivity::class.java))
    }
}