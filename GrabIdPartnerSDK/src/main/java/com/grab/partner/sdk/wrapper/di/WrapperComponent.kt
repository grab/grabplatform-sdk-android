/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */
package com.grab.partner.sdk.wrapper.di

import com.grab.partner.sdk.wrapper.chrometabmanager.ChromeManagerActivity
import com.grab.partner.sdk.wrapper.manager.GrabSdkManagerImpl
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [WrapperModule::class])
interface WrapperComponent {
    fun inject(manager: GrabSdkManagerImpl)
    fun inject(activity: ChromeManagerActivity)

}
