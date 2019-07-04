package com.grab.partner.sdk

import android.content.Context
import com.grab.partner.sdk.models.GrabIdPartnerError
import com.grab.partner.sdk.models.GrabIdPartnerErrorCode
import com.grab.partner.sdk.models.GrabIdPartnerErrorDomain
import com.grab.partner.sdk.models.IdTokenInfo
import com.grab.partner.sdk.models.LoginSession

internal open class StubGrabIdPartner : GrabIdPartnerProtocol {

    var callbackStatus: Boolean = false
    var loginSession: LoginSession = LoginSession()
    var idTokenInfo: IdTokenInfo = IdTokenInfo()
    var accessTokenStatus: Boolean = true
    var idTokenStatus: Boolean = true
    var sdkInitialized: Boolean = false
    private val testErrorMessage = "test_error_message"
    var settableError: GrabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.TEARDOWN, GrabIdPartnerErrorCode.errorInTearDown, testErrorMessage, null)

    override fun initialize(context: Context) {
        sdkInitialized = true
    }

    override fun loadLoginSession(callback: LoginSessionCallback) {
        if (callbackStatus) {
            callback.onError(settableError)
        } else {
            callback.onSuccess(loginSession)
        }
    }

    override fun loadLoginSession(state: String, clientId: String, redirectUri: String, serviceDiscoveryUrl: String, scope: String, acrValues: String?, request: String?, loginHint: String?, idTokenHint: String?, callback: LoginSessionCallback) {
        if (callbackStatus) {
            callback.onError(settableError)
        } else {
            callback.onSuccess(loginSession)
        }
    }

    override fun login(loginSession: LoginSession, context: Context, callback: LoginCallback) {
        if (callbackStatus) {
            callback.onError(settableError)
        } else {
            callback.onSuccess()
        }
    }

    override fun login(loginSession: LoginSession, context: Context, callback: LoginCallbackV2) {
        if (callbackStatus) {
            callback.onError(settableError)
        } else {
            callback.onSuccessCache()
        }
    }

    override fun exchangeToken(loginSession: LoginSession, redirectUrl: String, callback: ExchangeTokenCallback) {
        if (callbackStatus) {
            callback.onError(settableError)
        } else {
            callback.onSuccess()
        }
    }

    override fun getIdTokenInfo(loginSession: LoginSession, callback: GetIdTokenInfoCallback) {
        if (callbackStatus) {
            callback.onError(settableError)
        } else {
            callback.onSuccess(idTokenInfo)
        }
    }

    override fun logout(loginSession: LoginSession, callback: LogoutCallback) {
        if (callbackStatus) {
            callback.onError(settableError)
        } else {
            callback.onSuccess()
        }
    }

    override fun isValidAccessToken(loginSession: LoginSession): Boolean {
        return accessTokenStatus
    }

    override fun isValidIdToken(idTokenInfo: IdTokenInfo): Boolean {
        return idTokenStatus
    }

    override fun teardown() {
    }
}