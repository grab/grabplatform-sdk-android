package com.grab.partner.sdk.wrapper.di

import android.content.Context

import com.grab.partner.sdk.wrapper.deeplink.DeepLinkActivity
import com.grab.partner.sdk.wrapper.manager.GrabSdkManager
import com.grab.partner.sdk.wrapper.manager.GrabSdkManagerImpl

import javax.inject.Singleton

import dagger.BindsInstance
import dagger.Component

@Singleton
@Component(modules = [WrapperModule::class])
interface WrapperComponent {

    fun inject(deepLinkActivity: DeepLinkActivity)
    fun inject(manager: GrabSdkManagerImpl)

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun manager(manager: GrabSdkManagerImpl): Builder

        @BindsInstance
        fun context(context: Context): Builder

        fun build(): WrapperComponent
    }
}
