/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk.sampleapp.di.modules

import android.content.Context
import com.grab.partner.sdk.sampleapp.api.GrabAPI
import com.grab.partner.sdk.sampleapp.api.GrabRepository
import com.grab.partner.sdk.sampleapp.api.GrabRepositoryImpl
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
internal class APIModule {
    @Provides
    @Singleton
    fun provideGrabAuthRepository(context: Context, grabApi: GrabAPI): GrabRepository = GrabRepositoryImpl(context, grabApi)

    @Provides
    @Singleton
    fun provideGrabSdkApi(retrofit: Retrofit): GrabAPI = retrofit.create(GrabAPI::class.java)
}