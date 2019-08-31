/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk

import android.content.Context
import com.grab.partner.sdk.models.LoginSession
import com.grab.partner.sdk.utils.LaunchAppForAuthorization

class StubLaunchAppForAuthorization : LaunchAppForAuthorization {
    override fun launchOAuthFlow(context: Context, loginSession: LoginSession, callback: LoginCallback, shouldLaunchNativeApp: Boolean) {
        callback.onSuccess()
    }

    override fun speedUpChromeTabs() {
    }
}