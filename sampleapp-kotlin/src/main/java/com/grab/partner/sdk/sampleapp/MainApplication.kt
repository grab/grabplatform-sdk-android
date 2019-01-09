/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk.sampleapp

import android.app.Application
import com.grab.partner.sdk.GrabIdPartner
import com.grab.partner.sdk.sampleapp.di.components.DaggerSampleAppComponent
import com.grab.partner.sdk.sampleapp.di.components.SampleAppComponent
import com.grab.partner.sdk.sampleapp.di.modules.SampleAppModule

class MainApplication : Application() {
    lateinit var component: SampleAppComponent

    override fun onCreate() {
        super.onCreate()
        component = DaggerSampleAppComponent.builder()
                .sampleAppModule(SampleAppModule(this))
                .build()
        GrabIdPartner.instance.initialize(applicationContext)
    }
}