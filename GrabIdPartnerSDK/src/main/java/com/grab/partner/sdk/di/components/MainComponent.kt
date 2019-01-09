/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk.di.components

import com.grab.partner.sdk.GrabIdPartner
import com.grab.partner.sdk.di.modules.APIModule
import com.grab.partner.sdk.di.modules.AppModule
import com.grab.partner.sdk.di.modules.NetworkModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [
        AppModule::class,
        APIModule::class,
        NetworkModule::class]
)
internal interface MainComponent {
    fun inject(grabIdPartner: GrabIdPartner)
}