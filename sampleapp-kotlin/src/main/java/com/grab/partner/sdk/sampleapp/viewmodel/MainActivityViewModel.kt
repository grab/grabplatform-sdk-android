/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk.sampleapp.viewmodel

import android.app.Activity
import android.text.method.ScrollingMovementMethod
import android.view.View
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import com.grab.partner.sdk.GetIdTokenInfoCallback
import com.grab.partner.sdk.GrabIdPartner
import com.grab.partner.sdk.LogoutCallback
import com.grab.partner.sdk.models.GrabIdPartnerError
import com.grab.partner.sdk.models.IdTokenInfo
import com.grab.partner.sdk.models.LoginSession
import com.grab.partner.sdk.sampleapp.R
import com.grab.partner.sdk.sampleapp.api.GrabRepository
import com.grab.partner.sdk.sampleapp.models.UserInfoAPIResponse
import com.grab.partner.sdk.sampleapp.scheduleprovider.SchedulerProvider
import com.grab.partner.sdk.wrapper.manager.GrabSdkManager
import com.grab.partner.sdk.wrapper.manager.SessionCallbacks
import retrofit2.HttpException

class MainActivityViewModel(
    private val activity: Activity,
    private val grabRepository: GrabRepository,
    private val schedulerProvider: SchedulerProvider
) : SessionCallbacks {
    companion object {
        private var loginSession: LoginSession? = null
    }

    var progressBarVisibility = ObservableInt(View.GONE)
    var stringMessage = ObservableField<String>()
    var movementMethod: ScrollingMovementMethod = ScrollingMovementMethod()

    /**
     * To initiate the login process with Grab ID Partner SDK
     */
    fun clickLogin() {
        progressBarVisibility.set(View.VISIBLE)
        stringMessage.set("")
        val sdkManager = GrabSdkManager.Builder().clientId("[obtain from GrabId team]")
            .redirectURI("[obtain from GrabId team]")
            .scope("[obtain from GrabId team]")
            .serviceDiscoveryUrl("[obtain from GrabId team]")
            .listener(this)
            .exchangeRequired(true)
            .build(activity)

        sdkManager.doLogin(activity, "[obtain from GrabId team]")
    }

    /**
     * To initiate the verify ID token flow with Grab ID Partner SDK
     */
    fun getIdTokenInfo() {
        progressBarVisibility.set(View.VISIBLE)
        loginSession?.let { loginSession ->
            GrabIdPartner.instance.getIdTokenInfo(loginSession, object : GetIdTokenInfoCallback {
                override fun onSuccess(idTokenInfo: IdTokenInfo) {
                    stringMessage.set(createIdTokenResponse(idTokenInfo))
                    progressBarVisibility.set(View.GONE)
                }

                override fun onError(grabIdPartnerError: GrabIdPartnerError) {
                    setErrorState(grabIdPartnerError)
                }

            })
        } ?: run {
            stringMessage.set("Please initiate login flow first, loginSession is null")
            progressBarVisibility.set(View.GONE)
        }
    }

    /**
     * To initiate the logout/clear loginSession process
     */
    fun clearGrabSignInSession() {
        loginSession?.let {
            GrabIdPartner.instance.logout(it, object : LogoutCallback {
                override fun onSuccess() {
                    stringMessage.set("Successfully cleared loginSession for the user")
                }

                override fun onError(grabIdPartnerError: GrabIdPartnerError) {
                    setErrorState(grabIdPartnerError)
                }

            })
        }
    }

    /**
     * sample protected API call using the token received from Grab ID Partner SDK
     */
    fun accessProtectedResource() {
        grabRepository.getProtectedAPIResponse("BEARER " + loginSession?.accessToken)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe({ result ->
                    stringMessage.set(printUserInfoAPIResponse(result))
                }, { error ->
                    if (error is HttpException) {
                        val errorJsonString = error.response()?.errorBody()?.string()
                        stringMessage.set(activity.resources.getString(R.string.error_accessing_protected_resource) + " Error: $errorJsonString")
                    }
                    stringMessage.set(activity.resources.getString(R.string.error_accessing_protected_resource) + error.localizedMessage)
                })
    }

    private fun createTokenResponse(loginSession: LoginSession): String {
        var tokenResponseString = "---------------------------------------------------------------------------- \n" + "Response from oauth2/token" + "\n\n access_token: \n %s" + "\n\n id_token: \n %s" + "\n\n refresh_token: \n %s" + "\n\nexpires_in: \n %s"
        return String.format(tokenResponseString, loginSession.accessToken, loginSession.idToken, loginSession.refreshToken, loginSession.accessTokenExpiresAt)
    }

    private fun createIdTokenResponse(idTokenInfo: IdTokenInfo): String {
        var idTokenResponseString = "----------------------------------------------------------------------------\n" + "Response from oauth2/id_tokens/token_info" + "\n\n audience: \n %s" + "\n\n expiration: \n %s" + "\n\n issueDate: \n %s" + "\n\n issuer: \n %s" +
                "\n\n notValidBefore: \n %s" + "\n\n partnerId: \n %s" + "\n\n partnerUserId: \n %s" + "\n\n service: \n %s" + "\n\n tokenId: \n %s" + "\n\n nonce: \n %s"
        return String.format(idTokenResponseString, idTokenInfo.audience, idTokenInfo.expiration, idTokenInfo.issueDate, idTokenInfo.issuer, idTokenInfo.notValidBefore, idTokenInfo.partnerId, idTokenInfo.partnerUserId, idTokenInfo.service, idTokenInfo.tokenId, idTokenInfo.nonce)
    }

    private fun printUserInfoAPIResponse(userInfoAPIResponse: UserInfoAPIResponse): String {
        var userInfoResponseString = "---------------------------------------------------------------------------- \n" + "Response from oauth2/test_res API" + "\n\n userID: \n %s" + "\n\n serviceID: \n %s" + "\n\n serviceUserID: \n %s" + "\n\n authMethod: \n %s"
        return String.format(userInfoResponseString, userInfoAPIResponse.userID, userInfoAPIResponse.serviceID, userInfoAPIResponse.serviceUserID, userInfoAPIResponse.authMethod)
    }

    private fun setErrorState(grabIdPartnerError: GrabIdPartnerError){
        stringMessage.set("Localized Message: " + grabIdPartnerError.localizeMessage + "\nService error: " + grabIdPartnerError.serviceError?.localizedMessage)
        progressBarVisibility.set(View.GONE)
    }

    override fun onSuccess(loginSession: LoginSession) {
        MainActivityViewModel.loginSession = loginSession
        stringMessage.set(createTokenResponse(loginSession))
        progressBarVisibility.set(View.GONE)
    }

    override fun onError(grabIdPartnerError: GrabIdPartnerError) {
        setErrorState(grabIdPartnerError)
    }

    override fun onSuccessFromCache(loginSession: LoginSession) {
        MainActivityViewModel.loginSession = loginSession
        stringMessage.set(createTokenResponse(loginSession))
        progressBarVisibility.set(View.GONE)
    }
}