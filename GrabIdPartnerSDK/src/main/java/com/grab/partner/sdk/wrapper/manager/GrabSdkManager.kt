package com.grab.partner.sdk.wrapper.manager

import android.content.Context
import com.grab.partner.sdk.models.GrabIdPartnerError
import com.grab.partner.sdk.models.LoginSession

interface GrabSdkManager {
    fun init(context: Context)
    fun doLogin(context: Context, clientId: String)

    class Builder {

        var clientId: String = EMPTY_STRING_CONST
        var redirectURI: String = EMPTY_STRING_CONST
        var serviceDiscoveryUrl: String = EMPTY_STRING_CONST
        var scope: String = EMPTY_STRING_CONST
        // Used by app for one time transactions scenario - base64 encoded jwt
        var request: String? = EMPTY_STRING_CONST
        var loginHint: String? = EMPTY_STRING_CONST
        var idTokenHint: String? = EMPTY_STRING_CONST
        // The OpenID Connect ACR optional parameter to the authorize endpoint will be utilized to pass in
        // service id info and device ID
        var acrValues: String? = EMPTY_STRING_CONST

        var listener: SessionCallbacks? = null

        var exchangeRequired: Boolean = false
        lateinit var loginSession: LoginSession
        lateinit var state: String

        fun clientId(clientId: String): Builder {
            this.clientId = clientId
            return this
        }

        fun redirectURI(redirectURI: String): Builder {
            this.redirectURI = redirectURI
            return this
        }

        fun scope(scope: String): Builder {
            this.scope = scope
            return this
        }

        fun acrValues(acrValues: String): Builder {
            this.acrValues = acrValues
            return this
        }

        fun request(request: String): Builder {
            this.request = request
            return this
        }

        fun loginHint(loginHint: String): Builder {
            this.loginHint = loginHint
            return this
        }

        fun idTokenHint(idTokenHint: String): Builder {
            this.idTokenHint = idTokenHint
            return this
        }

        fun serviceDiscoveryUrl(serviceDiscoveryUrl: String): Builder {
            this.serviceDiscoveryUrl = serviceDiscoveryUrl
            return this
        }

        fun listener(listener: SessionCallbacks): Builder {
            this.listener = listener
            return this
        }

        fun exchangeRequired(exchangeRequired: Boolean): Builder {
            this.exchangeRequired = exchangeRequired
            return this
        }

        fun build(context: Context): GrabSdkManager {
            val manager = GrabSdkManagerImpl.manager
            manager.init(context)

            state = manager.utility.getRandomString()
            manager.sessions[state] = this
            manager.clientStates[clientId] = state
            return manager
        }
    }
}

interface SessionCallbacks {
    fun onSuccess(loginSession: LoginSession)
    fun onError(grabIdPartnerError: GrabIdPartnerError)
    fun onSuccessFromCache(loginSession: LoginSession)
}