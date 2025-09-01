/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */
package com.grab.partner.sdk.wrapper.manager

import android.content.Context
import com.grab.partner.sdk.StubGrabIdPartner
import com.grab.partner.sdk.models.LoginSession
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class GrabLoginApiImplTest {
    private val grabIdPartner = StubGrabIdPartner()
    private val grabLoginApi = GrabLoginApiImpl(grabIdPartner)
    private val loginSession: LoginSession = LoginSession()
    private val sessionCallbacks: SessionCallbacks = mock()
    private val builder: GrabSdkManager.Builder = GrabSdkManager.Builder()
    private val context: Context = mock()
    private val state = "STATE"

    @Before
    fun setUp() {
        builder.listener = sessionCallbacks
    }

    @Test
    fun `verify doLogin error`() {
        grabIdPartner.callbackStatus = true
        grabLoginApi.doLogin(context, state, builder)
        verify(sessionCallbacks).onError(any())
    }

    @Test
    fun `verify doLogin`() {
        grabIdPartner.callbackStatus = false
        grabLoginApi.doLogin(context, state, builder)
        verify(sessionCallbacks).onSuccessFromCache(any())
    }

    @Test
    fun `exchange token error`() {
        grabIdPartner.callbackStatus = true
        grabLoginApi.exchangeToken(loginSession, "testRedictUri", builder)
        verify(sessionCallbacks).onError(any())
    }

    @Test
    fun `exchange token success`() {
        grabIdPartner.callbackStatus = false
        grabLoginApi.exchangeToken(loginSession, "testRedictUri", builder)
        verify(sessionCallbacks).onSuccess(loginSession)
    }
}