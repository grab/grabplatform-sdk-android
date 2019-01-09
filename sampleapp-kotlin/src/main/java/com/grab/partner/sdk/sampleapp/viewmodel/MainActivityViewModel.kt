/*
 * Copyright (c) 2012-2019 Grab Taxi Holdings PTE LTD (GRAB), All Rights Reserved. NOTICE: All information contained herein is, and remains the property of GRAB. The intellectual and technical concepts contained herein are confidential, proprietary and controlled by GRAB and may be covered by patents, patents in process, and are protected by trade secret or copyright law.
 * You are strictly forbidden to copy, download, store (in any medium), transmit, disseminate, adapt or change this material in any way unless prior written permission is obtained from GRAB. Access to the source code contained herein is hereby forbidden to anyone except current GRAB employees or contractors with binding Confidentiality and Non-disclosure agreements explicitly covering such access.
 *
 * The copyright notice above does not evidence any actual or intended publication or disclosure of this source code, which includes information that is confidential and/or proprietary, and is a trade secret, of GRAB.
 * ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC PERFORMANCE, OR PUBLIC DISPLAY OF OR THROUGH USE OF THIS SOURCE CODE WITHOUT THE EXPRESS WRITTEN CONSENT OF GRAB IS STRICTLY PROHIBITED, AND IN VIOLATION OF APPLICABLE LAWS AND INTERNATIONAL TREATIES. THE RECEIPT OR POSSESSION OF THIS SOURCE CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY ANY RIGHTS TO REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR SELL ANYTHING THAT IT MAY DESCRIBE, IN WHOLE OR IN PART.
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