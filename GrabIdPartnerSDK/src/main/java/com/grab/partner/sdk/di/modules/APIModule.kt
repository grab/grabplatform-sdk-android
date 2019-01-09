/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk.di.modules

import com.grab.partner.sdk.api.GrabAuthRepository
import com.grab.partner.sdk.api.GrabAuthRepositoryImpl
import com.grab.partner.sdk.api.GrabSdkApi
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
internal class APIModule {
    @Provides
    @Singleton
    fun provideGrabAuthRepository(grabSdkApi: GrabSdkApi): GrabAuthRepository = GrabAuthRepositoryImpl(grabSdkApi)

    @Provides
    @Singleton
    fun provideGrabSdkApi(retrofit: Retrofit): GrabSdkApi = retrofit.create(GrabSdkApi::class.java)
}