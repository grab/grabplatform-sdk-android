/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk.sampleapp.di.modules

import android.app.Application
import android.content.Context
import com.grab.partner.sdk.sample.viewmodel.MainActivityViewModel
import com.grab.partner.sdk.sampleapp.api.GrabRepository
import com.grab.partner.sdk.sampleapp.scheduleprovider.SchedulerProvider
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class SampleAppModule(private val app: Application) {
    @Provides
    @Singleton
    fun provideContext(): Context = app

    @Provides
    @Singleton
    fun provideScheduleProvider(): SchedulerProvider = SchedulerProvider()

    @Provides
    @Singleton
    fun provideViewModel(context: Context, grabRepository: GrabRepository, schedulerProvider: SchedulerProvider): MainActivityViewModel = MainActivityViewModel(context, grabRepository, schedulerProvider)
}