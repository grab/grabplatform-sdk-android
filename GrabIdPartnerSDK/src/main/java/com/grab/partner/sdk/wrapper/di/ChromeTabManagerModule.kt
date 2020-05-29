/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */
package com.grab.partner.sdk.wrapper.di

import com.grab.partner.sdk.repo.GrabIdPartnerRepo
import com.grab.partner.sdk.repo.GrabIdPartnerRepoImpl
import com.grab.partner.sdk.utils.ChromeTabLauncher
import com.grab.partner.sdk.utils.ChromeTabLauncherImpl
import com.grab.partner.sdk.wrapper.chrometabmanager.ChromeManagerActivityLauncher
import com.grab.partner.sdk.wrapper.chrometabmanager.ChromeTabManagerLauncherImpl
import dagger.Module
import dagger.Provides
import dagger.Reusable

@Module
class ChromeTabManagerModule {
    @Provides
    @Reusable
    internal fun provideChromeTabManagerLauncher(): ChromeManagerActivityLauncher = ChromeTabManagerLauncherImpl()

    @Provides
    @Reusable
    internal fun provideGrabIdPartnerRepo(): GrabIdPartnerRepo = GrabIdPartnerRepoImpl()

    @Provides
    @Reusable
    internal fun provideChromeTabLauncher(): ChromeTabLauncher = ChromeTabLauncherImpl()
}