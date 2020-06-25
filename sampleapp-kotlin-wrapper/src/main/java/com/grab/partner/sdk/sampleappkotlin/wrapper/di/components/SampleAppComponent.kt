/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk.sampleappkotlin.wrapper.di.components

import com.grab.partner.sdk.sampleappkotlin.wrapper.di.modules.APIModule
import com.grab.partner.sdk.sampleappkotlin.wrapper.di.modules.NetworkModule
import com.grab.partner.sdk.sampleappkotlin.wrapper.di.modules.SampleAppModule
import com.grab.partner.sdk.sampleappkotlin.wrapper.views.MainActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [SampleAppModule::class, NetworkModule::class, APIModule::class])
interface SampleAppComponent{
    fun inject(target: MainActivity)
}