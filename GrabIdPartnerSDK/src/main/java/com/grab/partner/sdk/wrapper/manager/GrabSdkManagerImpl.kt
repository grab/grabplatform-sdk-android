/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */
package com.grab.partner.sdk.wrapper.manager

import android.content.Context
import com.grab.partner.sdk.*
import com.grab.partner.sdk.models.GrabIdPartnerError
import com.grab.partner.sdk.utils.IUtility
import com.grab.partner.sdk.wrapper.di.DaggerWrapperComponent
import com.grab.partner.sdk.wrapper.di.WrapperComponent
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import com.grab.partner.sdk.GrabIdPartner.Companion.RESPONSE_STATE
import com.grab.partner.sdk.GrabIdPartner.Companion.RESPONSE_TYPE
import com.grab.partner.sdk.models.GrabIdPartnerErrorCode
import com.grab.partner.sdk.models.GrabIdPartnerErrorDomain
import com.grab.partner.sdk.utils.LogUtils

const val EMPTY_STRING_CONST = ""
private const val DO_LOGIN_TAG = "doLogin"
private const val RETURN_RESULT_TAG = "returnResult"
private const val SDK_NOT_INITIALIZED = "GrabSdkManager is not initialized"

class GrabSdkManagerImpl private constructor() : GrabSdkManager {

    private object Holder {
        fun createInstance() = GrabSdkManagerImpl()
    }

    companion object {
        lateinit var component: WrapperComponent
        @Volatile
        private var initInvoked: Boolean = false
        @Volatile
        private var INSTANCE: GrabSdkManagerImpl? = null

        fun getInstance(): GrabSdkManagerImpl =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: Holder.createInstance().also { INSTANCE = it }
                }

        fun isInitialized(): Boolean {
            return INSTANCE != null && initInvoked
        }
    }

    @Inject lateinit var utility: IUtility
    @Inject lateinit var grabIdPartner: GrabIdPartnerProtocol
    @Inject lateinit var loginApi: GrabLoginApi
    @Inject lateinit var sessions: ConcurrentHashMap<String, GrabSdkManager.Builder>
    @Inject lateinit var clientStates: ConcurrentHashMap<String, String>
    private var context: Context? = null

    override fun init(context: Context) {
        component = DaggerWrapperComponent.builder().build()
        component.inject(this)
        this.context = context
        grabIdPartner.initialize(context)
        initInvoked = true
    }

    override fun doLogin(context: Context, clientId: String) {
        if (isInitialized()) {
            val state = clientStates[clientId]
            val builder = sessions[state]

            if (state != null && builder != null) {
                loginApi.doLogin(context, state, builder)
            }
        } else {
            LogUtils.debug(DO_LOGIN_TAG, SDK_NOT_INITIALIZED)
        }
    }

    internal fun returnResult(result: String) {
        if (isInitialized()) {
            val code = utility.getURLParam(RESPONSE_TYPE, result)
            val state = utility.getURLParam(RESPONSE_STATE, result)
            val builder = sessions[state]

            builder?.let {
                if (code == null || code.isEmpty()) {
                    builder.listener?.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.EXCHANGETOKEN, GrabIdPartnerErrorCode.invalidCode,
                            utility.readResourceString(context, R.string.ERROR_MISSING_CODE), null))
                    return
                }

                val loginSession = builder.loginSession
                loginSession.codeInternal = code

                if (builder.exchangeRequired) {
                    loginSession.let {
                        loginApi.exchangeToken(it, result, builder)
                    }
                } else {
                    if (builder.listener != null)
                        builder.listener?.onSuccess(loginSession)
                }
            }
        } else {
            LogUtils.debug(RETURN_RESULT_TAG, SDK_NOT_INITIALIZED)
        }
    }

    override fun teardown() {
        context = null
        grabIdPartner.teardown()
        sessions.clear()
        clientStates.clear()
        INSTANCE = null
    }
}
