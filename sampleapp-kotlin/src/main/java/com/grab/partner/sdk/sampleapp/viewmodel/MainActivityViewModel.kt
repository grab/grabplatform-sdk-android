/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk.sample.viewmodel

import android.content.Context
import com.grab.partner.sdk.ExchangeTokenCallback
import com.grab.partner.sdk.GetIdTokenInfoCallback
import com.grab.partner.sdk.GrabIdPartner
import com.grab.partner.sdk.LoginCallback
import com.grab.partner.sdk.LoginSessionCallback
import com.grab.partner.sdk.LogoutCallback
import com.grab.partner.sdk.models.GrabIdPartnerError
import com.grab.partner.sdk.models.IdTokenInfo
import com.grab.partner.sdk.models.LoginSession
import com.grab.partner.sdk.sampleapp.api.GrabRepository
import com.grab.partner.sdk.sampleapp.models.UserInfoAPIResponse
import com.grab.partner.sdk.sampleapp.scheduleprovider.SchedulerProvider
import com.grab.partner.sdk.sampleapp.utils.ERROR_ACCESSING_PROTECTED_RESOURCE
import retrofit2.HttpException

class MainActivityViewModel(var context: Context,
                            private val grabRepository: GrabRepository,
                            private val schedulerProvider: SchedulerProvider) {
    companion object {
        private var loginSession: LoginSession? = null
    }

    private lateinit var callBack: CallBack
    private lateinit var redirectUrl: String


    fun setCallBack(loginCallBack: CallBack) {
        this.callBack = loginCallBack
    }

    fun setRedirectUrl(url: String) {
        this.redirectUrl = url
    }

    /**
     * To initiate the login process with Grab ID Partner SDK
     */
    fun clickLogin() {
        GrabIdPartner.instance.loadLoginSession(object : LoginSessionCallback {
            override fun onSuccess(loginSession: LoginSession) {
                MainActivityViewModel.loginSession = loginSession
                GrabIdPartner.instance.login(loginSession, context, object : LoginCallback {
                    override fun onSuccess() {
                        if (!loginSession.accessToken.isEmpty()) {
                            callBack.printTokenExchangeResponse(loginSession)
                        }
                    }

                    override fun onError(grabIdPartnerError: GrabIdPartnerError) {
                        callBack.printMessage(grabIdPartnerError.localizeMessage ?: "")
                    }

                })
            }

            override fun onError(grabIdPartnerError: GrabIdPartnerError) {
                callBack.printMessage(grabIdPartnerError.localizeMessage ?: "")
            }
        })
    }

    /**
     * To initiate the exchange token flow with Grab ID Partner SDK
     */
    fun getToken() {
        if (loginSession != null) {
            loginSession?.let {
                GrabIdPartner.instance.exchangeToken(loginSession!!, redirectUrl, object : ExchangeTokenCallback {
                    override fun onSuccess() {
                        callBack.printTokenExchangeResponse(loginSession!!)
                    }

                    override fun onError(grabIdPartnerError: GrabIdPartnerError) {
                        callBack.printMessage(grabIdPartnerError.localizeMessage ?: "")
                    }

                })
            }
        } else {
            callBack.printMessage("Please initiate login flow first, loginSession is null")
        }
    }

    /**
     * To initiate the verify ID token flow with Grab ID Partner SDK
     */
    fun getIdTokenInfo() {
        loginSession?.let {
            GrabIdPartner.instance.getIdTokenInfo(loginSession!!, object : GetIdTokenInfoCallback {
                override fun onSuccess(idTokenInfo: IdTokenInfo) {
                    callBack.printIdTokenResponse(idTokenInfo)
                }

                override fun onError(grabIdPartnerError: GrabIdPartnerError) {
                    var errorMessage = "Error occurred in getIdTokenInfo API. \nError Message: " + grabIdPartnerError.localizeMessage
                    callBack.printMessage(errorMessage)
                }

            })
        } ?: callBack.printMessage("Please initiate login flow first, loginSession is null")
    }

    /**
     * To initiate the logout/clear loginSession process
     */
    fun clearGrabSignInSession() {
        var loginSession = MainActivityViewModel.loginSession
        loginSession?.let {
            GrabIdPartner.instance.logout(loginSession, object : LogoutCallback {
                override fun onSuccess() {
                    callBack.clearTextView()
                    callBack.printMessage("Successfully cleared loginSession for the user")
                }

                override fun onError(grabIdPartnerError: GrabIdPartnerError) {
                    callBack.printMessage(grabIdPartnerError.localizeMessage ?: "")
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
                    callBack.printUserInfoAPIResponse(result)
                }, { error ->
                    if (error is HttpException) {
                        val errorJsonString = error.response().errorBody()?.string()
                        callBack.printMessage("$ERROR_ACCESSING_PROTECTED_RESOURCE Error: $errorJsonString")
                    } else {
                    }
                    callBack.printMessage(ERROR_ACCESSING_PROTECTED_RESOURCE + error.localizedMessage)
                })
    }
}

interface CallBack {
    fun printTokenExchangeResponse(loginSession: LoginSession)
    fun printIdTokenResponse(idTokenInfo: IdTokenInfo)
    fun printUserInfoAPIResponse(userInfoAPIResponse: UserInfoAPIResponse)
    fun clearTextView()
    fun printMessage(message: String)
}