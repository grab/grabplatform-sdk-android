/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk.di.components

import android.content.SharedPreferences
import com.grab.partner.sdk.GrabIdPartner
import com.grab.partner.sdk.di.modules.APIModule
import com.grab.partner.sdk.di.modules.AppModule
import com.grab.partner.sdk.di.modules.NetworkModule
import com.grab.partner.sdk.repo.GrabIdPartnerRepo
import com.grab.partner.sdk.utils.ChromeTabLauncher
import com.grab.partner.sdk.utils.IUtility
import com.grab.partner.sdk.wrapper.chrometabmanager.ChromeManagerActivityLauncher
import com.grab.partner.sdk.wrapper.di.ChromeTabManagerModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AppModule::class,
    APIModule::class,
    NetworkModule::class,
    ChromeTabManagerModule::class]
)
internal interface MainComponent {
    fun inject(grabIdPartner: GrabIdPartner)
    fun getGrabIdPartnerRepo(): GrabIdPartnerRepo
    fun getChromeManagerActivityLauncher(): ChromeManagerActivityLauncher
    fun getChromeTabLauncher(): ChromeTabLauncher
    fun getUtility(): IUtility
    fun getSharedPreferences(): SharedPreferences
}