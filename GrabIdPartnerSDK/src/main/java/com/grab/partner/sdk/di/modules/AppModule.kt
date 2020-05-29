/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk.di.modules

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.grab.partner.sdk.keystore.AndroidKeyStoreWrapper
import com.grab.partner.sdk.keystore.CipherWrapper
import com.grab.partner.sdk.keystore.IAndroidKeyStoreWrapper
import com.grab.partner.sdk.keystore.ICipher
import com.grab.partner.sdk.repo.GrabIdPartnerRepo
import com.grab.partner.sdk.scheduleprovider.SchedulerProvider
import com.grab.partner.sdk.utils.ChromeTabLauncher
import com.grab.partner.sdk.utils.LaunchAppForAuthorizationImpl
import com.grab.partner.sdk.utils.LaunchAppForAuthorization
import com.grab.partner.sdk.utils.IUtility
import com.grab.partner.sdk.utils.Utility
import com.grab.partner.sdk.wrapper.chrometabmanager.ChromeManagerActivityLauncher
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
internal class AppModule(private var context: Context) {
    @Provides
    @Singleton
    fun provideLaunchAppForAuthorization(
        chromeTabLauncher: ChromeTabLauncher,
        chromeManagerActivityLauncher: ChromeManagerActivityLauncher,
        grabIdPartnerRepo: GrabIdPartnerRepo
    ): LaunchAppForAuthorization =
        LaunchAppForAuthorizationImpl(
            chromeTabLauncher,
            chromeManagerActivityLauncher,
            grabIdPartnerRepo
        )

    @Provides
    @Singleton
    fun provideScheduleProvider(): SchedulerProvider = SchedulerProvider()

    @Provides
    @Singleton
    fun provideContext(): Context = context

    @Provides
    @Singleton
    fun provideSharedPreference(context: Context): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    @Provides
    @Singleton
    fun provideUtility(): IUtility = Utility()

    @Provides
    @Singleton
    fun provideAndroidKeyStoreWrapper(): IAndroidKeyStoreWrapper = AndroidKeyStoreWrapper(context)

    @Provides
    @Singleton
    fun provideCipherWrapper(): ICipher = CipherWrapper()
}