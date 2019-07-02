package com.grab.partner.sdk.wrapper.manager

import android.content.Context
import android.util.Log
import com.grab.partner.sdk.*
import com.grab.partner.sdk.models.GrabIdPartnerError
import com.grab.partner.sdk.models.LoginSession

class GrabLoginApiImpl (private val grabIdPartner: GrabIdPartnerProtocol) : GrabLoginApi {

    override fun doLogin(context: Context, state: String, builder: GrabSdkManager.Builder) {

        grabIdPartner.loadLoginSession(state, builder.clientId, builder.redirectURI, builder.serviceDiscoveryUrl,
                builder.scope, builder.acrValues, builder.request, builder.loginHint,
                object : LoginSessionCallback {

                    override fun onSuccess(loginSession: LoginSession) {
                        builder.loginSession = loginSession
                        grabIdPartner.login(loginSession, context, object : LoginCallbackV2 {

                            override fun onSuccess() {

                            }

                            override fun onError(grabIdPartnerError: GrabIdPartnerError) {
                                if (builder.listener != null)
                                    builder.listener?.onError(grabIdPartnerError)
                            }

                            override fun onSuccessCache() {
                                if (builder.listener != null)
                                    builder.listener?.onSuccessFromCache(loginSession)
                            }
                        })
                    }

                    override fun onError(grabIdPartnerError: GrabIdPartnerError) {
                        if (builder.listener != null)
                            builder.listener?.onError(grabIdPartnerError)
                    }
                })
    }

    override fun exchangeToken(loginSession: LoginSession, redirectUrl: String, builder: GrabSdkManager.Builder) {
        grabIdPartner.exchangeToken(loginSession, redirectUrl,
                object : ExchangeTokenCallback {

                    override fun onSuccess() {
                        builder.listener?.onSuccess(loginSession)
                    }

                    override fun onError(grabIdPartnerError: GrabIdPartnerError) {
                        if (builder.listener != null) {
                            builder.listener?.onError(grabIdPartnerError)
                        }
                    }
                })
    }
}