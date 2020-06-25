/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk.sampleappkotlin.wrapper

import android.app.Application
import com.grab.partner.sdk.GrabIdPartner

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        GrabIdPartner.instance.initialize(applicationContext, null)
    }
}