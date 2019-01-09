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
import com.grab.partner.sdk.utils.IChromeCustomTab

class StubChromeCustomTab: IChromeCustomTab {
    override fun openChromeCustomTab(context: Context, loginSession: LoginSession, callback: LoginCallback) {
        callback.onSuccess()
    }

    override fun speedUpChromeTabs() {
    }
}