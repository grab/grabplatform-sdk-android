package com.grab.partner.sdk.wrapper.di

import com.grab.partner.sdk.wrapper.manager.GrabSdkManagerImpl
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [WrapperModule::class])
interface WrapperComponent {
    fun inject(manager: GrabSdkManagerImpl)
}
