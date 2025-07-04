package com.grab.partner.sdk.wrapper.manager

import android.content.Context
import com.grab.partner.sdk.GrabIdPartner
import com.grab.partner.sdk.GrabIdPartnerProtocol
import com.grab.partner.sdk.models.GrabIdPartnerError
import com.grab.partner.sdk.models.LoginSession
import com.grab.partner.sdk.utils.IUtility
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.junit.Before
import org.junit.Test
import java.util.concurrent.ConcurrentHashMap

class GrabSdkManagerImplTest {
    private lateinit var grabSdkManagerImpl: GrabSdkManagerImpl
    private val utility: IUtility = mock()
    private val sessions: ConcurrentHashMap<String, GrabSdkManager.Builder> = mock()
    private val clientStates: ConcurrentHashMap<String, String> = mock()
    private val builder: GrabSdkManager.Builder = GrabSdkManager.Builder()
    private val loginSession: LoginSession = LoginSession()
    private val sessionCallbacks: SessionCallbacks = mock()
    private val grabLoginApi: GrabLoginApi = mock()
    private val context: Context = mock()
    private val grabIdPartner: GrabIdPartnerProtocol = mock()
    private val clientId = "client_id"
    private val testResult = "TEST"
    private val code = "CODE"
    private val state = "STATE"

    @Before
    fun setup() {
        grabSdkManagerImpl = GrabSdkManagerImpl.getInstance()
        grabSdkManagerImpl.init(context)
        grabSdkManagerImpl.utility = utility
        grabSdkManagerImpl.sessions = sessions
        grabSdkManagerImpl.clientStates = clientStates
        grabSdkManagerImpl.loginApi = grabLoginApi
        grabSdkManagerImpl.grabIdPartner = grabIdPartner
    }

    @Test
    fun `verify doLogin`() {
        prerequisiteForDoLogin(state, builder)
        verify(grabLoginApi).doLogin(context, state, builder)
    }

    @Test
    fun `verify doLogin with state is null`() {
        prerequisiteForDoLogin(null, builder)
        verify(grabLoginApi, never()).doLogin(context, state, builder)
    }

    @Test
    fun `verify doLogin with builder is null`() {
        prerequisiteForDoLogin(clientId, null)
        verify(grabLoginApi, never()).doLogin(context, state, builder)
    }

    @Test
    fun `verify returnResult with exchangeRequired is false`() {
        prerequisiteForReturnResult(code, false)
        verify(sessionCallbacks).onSuccess(loginSession)
    }

    @Test
    fun `verify returnResult when exchangeRequired is true`() {
        prerequisiteForReturnResult(code, true)
        verify(grabLoginApi).exchangeToken(loginSession, testResult, builder)
    }

    @Test
    fun `verify returnResult when code is empty`() {
        val argumentCaptor = argumentCaptor<GrabIdPartnerError>()
        prerequisiteForReturnResult("", true)
        verify(sessionCallbacks).onError(argumentCaptor.capture())
    }

    @Test
    fun teardown() {
        grabSdkManagerImpl.teardown()
        verify(grabSdkManagerImpl.grabIdPartner).teardown()
        verify(grabSdkManagerImpl.sessions).clear()
        verify(grabSdkManagerImpl.clientStates).clear()
    }

    private fun prerequisiteForReturnResult(code: String, exchangeRequired: Boolean) {
        builder.loginSession = loginSession
        builder.listener = sessionCallbacks
        builder.exchangeRequired = exchangeRequired
        whenever(grabSdkManagerImpl.utility.getURLParam(GrabIdPartner.RESPONSE_TYPE, testResult)).thenReturn(code)
        whenever(grabSdkManagerImpl.utility.getURLParam(GrabIdPartner.RESPONSE_STATE, testResult)).thenReturn(state)
        whenever(grabSdkManagerImpl.sessions[state]).thenReturn(builder)
        whenever(grabSdkManagerImpl.clientStates[clientId]).thenReturn(state)
        grabSdkManagerImpl.returnResult(testResult)
    }

    private fun prerequisiteForDoLogin(state: String?, builder: GrabSdkManager.Builder?) {
        whenever(grabSdkManagerImpl.clientStates[clientId]).thenReturn(state)
        whenever(grabSdkManagerImpl.sessions[state]).thenReturn(builder)
        grabSdkManagerImpl.doLogin(context, clientId)
    }
}