/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */
package com.grab.partner.sdk.wrapper.di

import com.grab.partner.sdk.GrabIdPartner
import com.grab.partner.sdk.GrabIdPartnerProtocol
import com.grab.partner.sdk.utils.IUtility
import com.grab.partner.sdk.utils.Utility
import com.grab.partner.sdk.wrapper.manager.GrabLoginApi
import com.grab.partner.sdk.wrapper.manager.GrabLoginApiImpl
import com.grab.partner.sdk.wrapper.manager.GrabSdkManager
import dagger.Module
import dagger.Provides
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Singleton

@Module
class WrapperModule {

    @Provides
    @Singleton
    fun providesGrabIdPartnerProtocol(): GrabIdPartnerProtocol {
        return GrabIdPartner.instance
    }

    @Provides
    @Singleton
    fun providesSessions(): ConcurrentHashMap<String, GrabSdkManager.Builder> {
        return ConcurrentHashMap()
    }

    @Provides
    @Singleton
    fun providesClientStates(): ConcurrentHashMap<String, String> {
        return ConcurrentHashMap()
    }


    @Provides
    @Singleton
    fun provideUtility(): IUtility {
        return Utility()
    }

    @Provides
    @Singleton
    fun providesGrabLoginApi(grabIdPartner: GrabIdPartnerProtocol): GrabLoginApi =
            GrabLoginApiImpl(grabIdPartner)
}
