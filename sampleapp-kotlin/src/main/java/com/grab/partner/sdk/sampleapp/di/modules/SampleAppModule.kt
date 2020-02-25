/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk.sampleapp.di.modules

import android.app.Activity
import android.content.Context
import com.grab.partner.sdk.sampleapp.api.GrabRepository
import com.grab.partner.sdk.sampleapp.scheduleprovider.SchedulerProvider
import com.grab.partner.sdk.sampleapp.viewmodel.MainActivityViewModel
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class SampleAppModule(private val activity: Activity) {
    @Provides
    @Singleton
    fun provideContext(): Context = activity

    @Provides
    @Singleton
    fun provideScheduleProvider(): SchedulerProvider = SchedulerProvider()

    @Provides
    @Singleton
    fun provideViewModel(grabRepository: GrabRepository, schedulerProvider: SchedulerProvider): MainActivityViewModel = MainActivityViewModel(activity, grabRepository, schedulerProvider)
}