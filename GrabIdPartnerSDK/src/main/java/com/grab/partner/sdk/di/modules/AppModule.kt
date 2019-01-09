/*
 * Copyright (c) 2012-2019 Grab Taxi Holdings PTE LTD (GRAB), All Rights Reserved. NOTICE: All information contained herein is, and remains the property of GRAB. The intellectual and technical concepts contained herein are confidential, proprietary and controlled by GRAB and may be covered by patents, patents in process, and are protected by trade secret or copyright law.
 * You are strictly forbidden to copy, download, store (in any medium), transmit, disseminate, adapt or change this material in any way unless prior written permission is obtained from GRAB. Access to the source code contained herein is hereby forbidden to anyone except current GRAB employees or contractors with binding Confidentiality and Non-disclosure agreements explicitly covering such access.
 *
 * The copyright notice above does not evidence any actual or intended publication or disclosure of this source code, which includes information that is confidential and/or proprietary, and is a trade secret, of GRAB.
 * ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC PERFORMANCE, OR PUBLIC DISPLAY OF OR THROUGH USE OF THIS SOURCE CODE WITHOUT THE EXPRESS WRITTEN CONSENT OF GRAB IS STRICTLY PROHIBITED, AND IN VIOLATION OF APPLICABLE LAWS AND INTERNATIONAL TREATIES. THE RECEIPT OR POSSESSION OF THIS SOURCE CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY ANY RIGHTS TO REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR SELL ANYTHING THAT IT MAY DESCRIBE, IN WHOLE OR IN PART.
 */

package com.grab.partner.sdk.di.modules

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.grab.partner.sdk.keystore.AndroidKeyStoreWrapper
import com.grab.partner.sdk.keystore.CipherWrapper
import com.grab.partner.sdk.keystore.IAndroidKeyStoreWrapper
import com.grab.partner.sdk.keystore.ICipher
import com.grab.partner.sdk.scheduleprovider.SchedulerProvider
import com.grab.partner.sdk.utils.ChromeCustomTab
import com.grab.partner.sdk.utils.IChromeCustomTab
import com.grab.partner.sdk.utils.IUtility
import com.grab.partner.sdk.utils.Utility
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
internal class AppModule(private var context: Context) {
    @Provides
    @Singleton
    fun provideChromeCustomTab(): IChromeCustomTab = ChromeCustomTab()

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
    fun provideUtility(): IUtility = Utility(context)

    @Provides
    @Singleton
    fun provideAndroidKeyStoreWrapper(): IAndroidKeyStoreWrapper = AndroidKeyStoreWrapper(context)

    @Provides
    @Singleton
    fun provideCipherWrapper(): ICipher = CipherWrapper()
}