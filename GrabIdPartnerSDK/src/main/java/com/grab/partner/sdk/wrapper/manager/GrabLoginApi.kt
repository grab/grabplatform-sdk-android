package com.grab.partner.sdk.wrapper.manager

import android.content.Context
import com.grab.partner.sdk.models.LoginSession

interface GrabLoginApi {
    fun doLogin(context: Context, state: String, builder: GrabSdkManager.Builder)
    fun exchangeToken(loginSession: LoginSession, redirectUrl: String, builder: GrabSdkManager.Builder)
}