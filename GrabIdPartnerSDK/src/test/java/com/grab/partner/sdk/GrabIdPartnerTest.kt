/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import com.grab.partner.sdk.models.*
import com.grab.partner.sdk.scheduleprovider.TestSchedulerProvider
import com.grab.partner.sdk.utils.LaunchAppForAuthorization
import org.mockito.kotlin.*
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.internal.verification.Times
import java.security.KeyPair
import java.util.UUID

class GrabIdPartnerTest {
    private val grabIdPartner = GrabIdPartner.instance as GrabIdPartner
    private val loginSessionCallback = TestLoginSessionCallback()
    private val testLoginCallback = TestLoginCallback()
    private val testLoginCallbackV2 = TestLoginCallbackV2()
    private val testGetIdTokenInfoCallback = TestGetIdTokenInfoCallback()
    private val loginSession = LoginSession()
    private val utility = StubUtility()
    private val context: Context = mock(Context::class.java)
    private val activity: Activity = mock()
    private val sharedPreferences = mock(SharedPreferences::class.java)
    private val packageManager = mock(PackageManager::class.java)
    private val grabAuthRepository = StubGrabAuthRepository()
    private val scheduleProvider = TestSchedulerProvider()
    private val launchAppForAuthorization = mock<LaunchAppForAuthorization>()
    private val keyPair = mock<KeyPair>()
    private val androidKeyStoreWrapper = StubAndroidKeyStoreWrapper(keyPair)
    private val cipherWrapper = StubCipherWrapper()
    private val compositeDisposable: CompositeDisposable = mock(CompositeDisposable::class.java)

    private val FAKE_CLIENT_ID = "fake_client_id"
    private val FAKE_REDIRECT_URI = "fake_redirectUri"
    private val FAKE_DISCOVERY_URL = "fake_discovery_url"
    private val FAKE_AUTH_ENDPOINT = "fake_auth_endpoint"
    private val FAKE_TOKEN_ENDPOINT = "fake_token_endpoint"
    private val FAKE_CLIENT_PUBLIC_INFO_ENDPOINT = "fake_client_public_info_endpoint {client_id}"
    private val FAKE_CLIENT_PUBLIC_INFO_ENDPOINT_WITH_CLIENT_ID = "fake_client_public_info_endpoint fake_client_id"
    private val FAKE_ID_TOKEN_ENDPOINT = "fake_id_token_verification_endpoint"
    private val PARTNER_SCOPE = "scope"
    private val TEST_RESPONSE_TYPE = "code"
    private val TEST_RESPONSE_STATE = "state"
    private val TEST_ACCESS_TOKEN = "access_token"
    private val TEST_ID_TOKEN = "id_token"
    private val TEST_NONCE = "nonce"
    private val TEST_EXPIRY_TIME = "100"
    private val LOGIN_HINT = "login_hint"
    private val ID_TOKEN_HINT = "id_token_hint"

    @Before
    fun setUp() {
        testLoginCallback.reset()
        loginSessionCallback.reset()
        grabIdPartner.utility = utility
        grabIdPartner.grabAuthRepository = grabAuthRepository
        grabIdPartner.schedulerProvider = scheduleProvider
        grabIdPartner.sharedPreferences = sharedPreferences
        grabIdPartner.launchAppForAuthorization = launchAppForAuthorization
        grabIdPartner.androidKeyStoreWrapper = androidKeyStoreWrapper
        grabIdPartner.cipherWrapper = cipherWrapper
        grabIdPartner.compositeDisposable = compositeDisposable
    }

    @Test
    fun `verify loadLoginSession without initializing the SDK`() {
        GrabIdPartner.isSdkInitialized = false
        grabIdPartner.loadLoginSession(loginSessionCallback)

        val grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.LOADLOGINSESSION, GrabIdPartnerErrorCode.sdkNotInitialized, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(loginSessionCallback.verifyOnError(grabIdPartnerError))
    }

    @Test
    fun `verify loadLoginSession2 without initializing the SDK`() {
        GrabIdPartner.isSdkInitialized = false
        grabIdPartner.loadLoginSession(
            TEST_RESPONSE_STATE,
            FAKE_CLIENT_ID,
            FAKE_REDIRECT_URI,
            FAKE_DISCOVERY_URL,
            PARTNER_SCOPE,
            null,
            null,
            null,
            null,
            loginSessionCallback,
            null,
            true
        )
        val grabIdPartnerError = GrabIdPartnerError(
            GrabIdPartnerErrorDomain.LOADLOGINSESSION,
            GrabIdPartnerErrorCode.sdkNotInitialized,
            CONST_READ_RESOURCE_STRING,
            null
        )
        Assert.assertTrue(loginSessionCallback.verifyOnError(grabIdPartnerError))
    }

    @Test
    fun `verify loadLoginSession with null clientId`() {
        prerequisiteToValidateLoadLoginSessionWithDifferentPartnerinfo(null, null, null, null)
        val grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.LOADLOGINSESSION, GrabIdPartnerErrorCode.invalidClientId, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(loginSessionCallback.verifyOnError(grabIdPartnerError))
    }

    @Test
    fun `verify loadLoginSession with empty clientId`() {
        prerequisiteToValidateLoadLoginSessionWithDifferentPartnerinfo("", null, null, null)
        val grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.LOADLOGINSESSION, GrabIdPartnerErrorCode.invalidClientId, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(loginSessionCallback.verifyOnError(grabIdPartnerError))
    }

    @Test
    fun `verify loadLoginSession2 with empty clientId`() {
        GrabIdPartner.isSdkInitialized = true
        grabIdPartner.loadLoginSession(
            TEST_RESPONSE_STATE,
            "",
            FAKE_REDIRECT_URI,
            FAKE_DISCOVERY_URL,
            PARTNER_SCOPE,
            null,
            null,
            null,
            null,
            loginSessionCallback,
            null,
            true
        )
        val grabIdPartnerError = GrabIdPartnerError(
            GrabIdPartnerErrorDomain.LOADLOGINSESSION,
            GrabIdPartnerErrorCode.invalidClientId,
            CONST_READ_RESOURCE_STRING,
            null
        )
        Assert.assertTrue(loginSessionCallback.verifyOnError(grabIdPartnerError))
    }

    @Test
    fun `verify loadLoginSession with null redirectUri`() {
        prerequisiteToValidateLoadLoginSessionWithDifferentPartnerinfo(FAKE_CLIENT_ID, null, null, null)
        val grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.LOADLOGINSESSION, GrabIdPartnerErrorCode.invalidRedirectURI, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(loginSessionCallback.verifyOnError(grabIdPartnerError))
    }

    @Test
    fun `verify loadLoginSession with empty redirectUri`() {
        prerequisiteToValidateLoadLoginSessionWithDifferentPartnerinfo(FAKE_CLIENT_ID, "", null, null)
        val grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.LOADLOGINSESSION, GrabIdPartnerErrorCode.invalidRedirectURI, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(loginSessionCallback.verifyOnError(grabIdPartnerError))
    }

    @Test
    fun `verify loadLoginSession2 with empty redirectUri`() {
        GrabIdPartner.isSdkInitialized = true
        grabIdPartner.loadLoginSession(
            TEST_RESPONSE_STATE,
            FAKE_CLIENT_ID,
            "",
            FAKE_DISCOVERY_URL,
            PARTNER_SCOPE,
            null,
            null,
            null,
            null,
            loginSessionCallback,
            null,
            true
        )
        val grabIdPartnerError = GrabIdPartnerError(
            GrabIdPartnerErrorDomain.LOADLOGINSESSION,
            GrabIdPartnerErrorCode.invalidRedirectURI,
            CONST_READ_RESOURCE_STRING,
            null
        )
        Assert.assertTrue(loginSessionCallback.verifyOnError(grabIdPartnerError))
    }

    @Test
    fun `verify loadLoginSession with null serviceDiscoveryUrl`() {
        prerequisiteToValidateLoadLoginSessionWithDifferentPartnerinfo(FAKE_CLIENT_ID, FAKE_REDIRECT_URI, null, null)
        val grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.LOADLOGINSESSION, GrabIdPartnerErrorCode.invalidDiscoveryEndpoint, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(loginSessionCallback.verifyOnError(grabIdPartnerError))
    }

    @Test
    fun `verify loadLoginSession with empty serviceDiscoveryUrl`() {
        prerequisiteToValidateLoadLoginSessionWithDifferentPartnerinfo(FAKE_CLIENT_ID, FAKE_REDIRECT_URI, "", null)
        val grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.LOADLOGINSESSION, GrabIdPartnerErrorCode.invalidDiscoveryEndpoint, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(loginSessionCallback.verifyOnError(grabIdPartnerError))
    }

    @Test
    fun `verify loadLoginSession2 with empty serviceDiscoveryUrl`() {
        GrabIdPartner.isSdkInitialized = true
        grabIdPartner.loadLoginSession(
            TEST_RESPONSE_STATE,
            FAKE_CLIENT_ID,
            FAKE_REDIRECT_URI,
            "",
            PARTNER_SCOPE,
            null,
            null,
            null,
            null,
            loginSessionCallback,
            null,
            true
        )
        val grabIdPartnerError = GrabIdPartnerError(
            GrabIdPartnerErrorDomain.LOADLOGINSESSION,
            GrabIdPartnerErrorCode.invalidDiscoveryEndpoint,
            CONST_READ_RESOURCE_STRING,
            null
        )
        Assert.assertTrue(loginSessionCallback.verifyOnError(grabIdPartnerError))
    }

    @Test
    fun `verify loadLoginSession with null scope`() {
        prerequisiteToValidateLoadLoginSessionWithDifferentPartnerinfo(FAKE_CLIENT_ID, FAKE_REDIRECT_URI, FAKE_DISCOVERY_URL, null)
        val grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.LOADLOGINSESSION, GrabIdPartnerErrorCode.invalidPartnerScope, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(loginSessionCallback.verifyOnError(grabIdPartnerError))
    }

    @Test
    fun `verify loadLoginSession with empty scope`() {
        prerequisiteToValidateLoadLoginSessionWithDifferentPartnerinfo(FAKE_CLIENT_ID, FAKE_REDIRECT_URI, FAKE_DISCOVERY_URL, "")
        val grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.LOADLOGINSESSION, GrabIdPartnerErrorCode.invalidPartnerScope, CONST_READ_RESOURCE_STRING, null)
        loginSessionCallback.verifyOnSuccess(0)
        Assert.assertTrue(loginSessionCallback.verifyOnError(grabIdPartnerError))
    }

    @Test
    fun `verify loadLoginSession2 with empty scope`() {
        GrabIdPartner.isSdkInitialized = true
        grabIdPartner.loadLoginSession(
            TEST_RESPONSE_STATE,
            FAKE_CLIENT_ID,
            FAKE_REDIRECT_URI,
            FAKE_DISCOVERY_URL,
            "",
            null,
            null,
            null,
            null,
            loginSessionCallback,
            null,
            true
        )
        val grabIdPartnerError = GrabIdPartnerError(
            GrabIdPartnerErrorDomain.LOADLOGINSESSION,
            GrabIdPartnerErrorCode.invalidPartnerScope,
            CONST_READ_RESOURCE_STRING,
            null
        )
        Assert.assertTrue(loginSessionCallback.verifyOnError(grabIdPartnerError))
    }

    @Test
    fun `verify loadLoginSession with required partner info`() {
        prerequisiteToValidateLoadLoginSessionWithDifferentPartnerinfo(FAKE_CLIENT_ID, FAKE_REDIRECT_URI, FAKE_DISCOVERY_URL, PARTNER_SCOPE)
        loginSessionCallback.verifyOnSuccess(1)
    }

    @Test
    fun `verify loadLoginSession2 with required partner info`() {
        GrabIdPartner.isSdkInitialized = true
        grabIdPartner.loadLoginSession(
            TEST_RESPONSE_STATE,
            FAKE_CLIENT_ID,
            FAKE_REDIRECT_URI,
            FAKE_DISCOVERY_URL,
            PARTNER_SCOPE,
            null,
            null,
            null,
            null,
            loginSessionCallback,
            null,
            true
        )
        loginSessionCallback.verifyOnSuccess(1)
    }

    @Test
    fun `verify loginSession has required parameters after loadLoginSession`() {
        prerequisiteToValidateLoadLoginSessionWithDifferentPartnerinfo(FAKE_CLIENT_ID, FAKE_REDIRECT_URI, FAKE_DISCOVERY_URL, PARTNER_SCOPE)
        loginSessionCallback.verifyOnSuccess(1)
        loginSessionCallback.verifyAllRequiredParametersExists()
    }

    @Test
    fun `verify loginSession has required parameters after loadLoginSession2`() {
        GrabIdPartner.isSdkInitialized = true
        grabIdPartner.loadLoginSession(
            TEST_RESPONSE_STATE,
            FAKE_CLIENT_ID,
            FAKE_REDIRECT_URI,
            FAKE_DISCOVERY_URL,
            PARTNER_SCOPE,
            null,
            null,
            null,
            null,
            loginSessionCallback,
            null,
            true
        )
        loginSessionCallback.verifyOnSuccess(1)
        loginSessionCallback.verifyAllRequiredParametersExists()
    }

    @Test
    fun `verify login with empty client id`() {
        prerequisiteToValidateLogin("", "", "", "")

        val grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.LOGIN, GrabIdPartnerErrorCode.invalidClientId, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(testLoginCallback.verifyOnError(grabIdPartnerError))
        testLoginCallback.verifyOnSuccess(0)
    }

    @Test
    fun `verify loginV2 with empty client id`() {
        prerequisiteToValidateLogin("", "", "", "", true)

        val grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.LOGIN, GrabIdPartnerErrorCode.invalidClientId, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(testLoginCallbackV2.verifyOnError(grabIdPartnerError))
        testLoginCallbackV2.verifyOnSuccess(0)
    }

    @Test
    fun `verify login with empty redirectUri`() {
        prerequisiteToValidateLogin(FAKE_CLIENT_ID, "", "", "")

        val grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.LOGIN, GrabIdPartnerErrorCode.invalidRedirectURI, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(testLoginCallback.verifyOnError(grabIdPartnerError))
        testLoginCallback.verifyOnSuccess(0)
    }

    @Test
    fun `verify loginV2 with empty redirectUri`() {
        prerequisiteToValidateLogin(FAKE_CLIENT_ID, "", "", "", true)

        val grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.LOGIN, GrabIdPartnerErrorCode.invalidRedirectURI, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(testLoginCallbackV2.verifyOnError(grabIdPartnerError))
        testLoginCallbackV2.verifyOnSuccess(0)
    }

    @Test
    fun `verify login with empty scope`() {
        prerequisiteToValidateLogin(FAKE_CLIENT_ID, FAKE_REDIRECT_URI, "", "")

        val grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.LOGIN, GrabIdPartnerErrorCode.invalidPartnerScope, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(testLoginCallback.verifyOnError(grabIdPartnerError))
        testLoginCallback.verifyOnSuccess(0)
    }

    @Test
    fun `verify loginV2 with empty scope`() {
        prerequisiteToValidateLogin(FAKE_CLIENT_ID, FAKE_REDIRECT_URI, "", "", true)

        val grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.LOGIN, GrabIdPartnerErrorCode.invalidPartnerScope, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(testLoginCallbackV2.verifyOnError(grabIdPartnerError))
        testLoginCallbackV2.verifyOnSuccess(0)
    }

    @Test
    fun `verify login with empty serviceDiscoveryUrl`() {
        prerequisiteToValidateLogin(FAKE_CLIENT_ID, FAKE_REDIRECT_URI, PARTNER_SCOPE, "")

        val grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.LOGIN, GrabIdPartnerErrorCode.invalidDiscoveryEndpoint, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(testLoginCallback.verifyOnError(grabIdPartnerError))
        testLoginCallback.verifyOnSuccess(0)

    }

    @Test
    fun `verify loginV2 with empty serviceDiscoveryUrl`() {
        prerequisiteToValidateLogin(FAKE_CLIENT_ID, FAKE_REDIRECT_URI, PARTNER_SCOPE, "", true)

        val grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.LOGIN, GrabIdPartnerErrorCode.invalidDiscoveryEndpoint, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(testLoginCallbackV2.verifyOnError(grabIdPartnerError))
        testLoginCallbackV2.verifyOnSuccess(0)

    }

    @Test
    fun `verify login with no cache and empty authorization_endpoint`() {
        prerequisiteToValidateCallDiscovery("", "", "", "")

        val grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.SERVICEDISCOVERY, GrabIdPartnerErrorCode.errorInDiscoveryEndpoint, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(testLoginCallback.verifyOnError(grabIdPartnerError))
        testLoginCallback.verifyOnSuccess(0)
    }

    @Test
    fun `verify loginV2 with no cache and empty authorization_endpoint`() {
        prerequisiteToValidateCallDiscovery("", "", "", "", isV2Api = true)

        val grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.SERVICEDISCOVERY, GrabIdPartnerErrorCode.errorInDiscoveryEndpoint, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(testLoginCallbackV2.verifyOnError(grabIdPartnerError))
        testLoginCallbackV2.verifyOnSuccess(0)
    }

    @Test
    fun `verify login with no cache and empty token_endpoint`() {
        prerequisiteToValidateCallDiscovery(FAKE_AUTH_ENDPOINT, "", "", "")

        val grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.SERVICEDISCOVERY, GrabIdPartnerErrorCode.errorInDiscoveryEndpoint, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(testLoginCallback.verifyOnError(grabIdPartnerError))
        testLoginCallback.verifyOnSuccess(0)
    }

    @Test
    fun `verify loginV2 with no cache and empty token_endpoint`() {
        prerequisiteToValidateCallDiscovery(FAKE_AUTH_ENDPOINT, "", "", "", isV2Api = true)

        val grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.SERVICEDISCOVERY, GrabIdPartnerErrorCode.errorInDiscoveryEndpoint, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(testLoginCallbackV2.verifyOnError(grabIdPartnerError))
        testLoginCallbackV2.verifyOnSuccess(0)
    }

    @Test
    fun `verify login with no cache and empty id_token_verification_endpoint`() {
        prerequisiteToValidateCallDiscovery(FAKE_AUTH_ENDPOINT, FAKE_TOKEN_ENDPOINT, "", FAKE_CLIENT_PUBLIC_INFO_ENDPOINT)
        val grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.SERVICEDISCOVERY, GrabIdPartnerErrorCode.errorInDiscoveryEndpoint, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(testLoginCallback.verifyOnError(grabIdPartnerError))
        testLoginCallback.verifyOnSuccess(0)
    }

    @Test
    fun `verify loginV2 with no cache and empty id_token_verification_endpoint`() {
        prerequisiteToValidateCallDiscovery(FAKE_AUTH_ENDPOINT, FAKE_TOKEN_ENDPOINT, "", FAKE_CLIENT_PUBLIC_INFO_ENDPOINT, isV2Api = true)
        val grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.SERVICEDISCOVERY, GrabIdPartnerErrorCode.errorInDiscoveryEndpoint, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(testLoginCallbackV2.verifyOnError(grabIdPartnerError))
        testLoginCallbackV2.verifyOnSuccess(0)
    }

    @Test
    fun `verify callDiscovery updating the client_public_info_endpoint endpoint properly`() {
        prerequisiteToValidateCallDiscovery(auth_endpoint = FAKE_AUTH_ENDPOINT, token_endpoint = FAKE_TOKEN_ENDPOINT, id_token_verification_endpoint = FAKE_ID_TOKEN_ENDPOINT, client_public_info_endpoint = FAKE_CLIENT_PUBLIC_INFO_ENDPOINT)
        Assert.assertEquals(FAKE_CLIENT_PUBLIC_INFO_ENDPOINT_WITH_CLIENT_ID, grabAuthRepository.getClientInfoEndpointUrl())
    }

    @Test
    fun `verify callDiscovery updating the client_public_info_endpoint endpoint properly using the loginV2 api`() {
        prerequisiteToValidateCallDiscovery(auth_endpoint = FAKE_AUTH_ENDPOINT, token_endpoint = FAKE_TOKEN_ENDPOINT, id_token_verification_endpoint = FAKE_ID_TOKEN_ENDPOINT, client_public_info_endpoint = FAKE_CLIENT_PUBLIC_INFO_ENDPOINT, isV2Api = true)
        Assert.assertEquals(FAKE_CLIENT_PUBLIC_INFO_ENDPOINT_WITH_CLIENT_ID, grabAuthRepository.getClientInfoEndpointUrl())
    }

    @Test
    fun `verify login with no cache and empty client_public_info_endpoint return the specific error`() {
        loginSession.clientId = FAKE_CLIENT_ID
        loginSession.redirectUri = FAKE_REDIRECT_URI
        loginSession.scope = PARTNER_SCOPE
        loginSession.serviceDiscoveryUrl = FAKE_DISCOVERY_URL
        utility.setObjectToSharedPref(null)
        grabAuthRepository.setCallDiscovery(Observable.just(DiscoveryResponse(FAKE_AUTH_ENDPOINT, FAKE_TOKEN_ENDPOINT, FAKE_ID_TOKEN_ENDPOINT, FAKE_CLIENT_PUBLIC_INFO_ENDPOINT)))
        grabAuthRepository.setFetchClientPublicInfo(Observable.error(Throwable()))

        grabIdPartner.login(loginSession, context, testLoginCallback)
        val grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.CLIENTPUBLICINFO, GrabIdPartnerErrorCode.errorInClientPublicInfoEndpoint, "$CONST_READ_RESOURCE_STRING $CONST_READ_RESOURCE_STRING $FAKE_CLIENT_PUBLIC_INFO_ENDPOINT_WITH_CLIENT_ID", null)
        Assert.assertTrue(testLoginCallback.verifyOnError(grabIdPartnerError))
        testLoginCallback.verifyOnSuccess(0)
    }

    @Test
    fun `verify loginV2 with no cache and empty client_public_info_endpoint return the specific error`() {
        loginSession.clientId = FAKE_CLIENT_ID
        loginSession.redirectUri = FAKE_REDIRECT_URI
        loginSession.scope = PARTNER_SCOPE
        loginSession.serviceDiscoveryUrl = FAKE_DISCOVERY_URL
        utility.setObjectToSharedPref(null)
        grabAuthRepository.setCallDiscovery(Observable.just(DiscoveryResponse(FAKE_AUTH_ENDPOINT, FAKE_TOKEN_ENDPOINT, FAKE_ID_TOKEN_ENDPOINT, FAKE_CLIENT_PUBLIC_INFO_ENDPOINT)))
        grabAuthRepository.setFetchClientPublicInfo(Observable.error(Throwable()))

        grabIdPartner.loginV2(loginSession, activity, testLoginCallbackV2)
        val grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.CLIENTPUBLICINFO, GrabIdPartnerErrorCode.errorInClientPublicInfoEndpoint, "$CONST_READ_RESOURCE_STRING $CONST_READ_RESOURCE_STRING $FAKE_CLIENT_PUBLIC_INFO_ENDPOINT_WITH_CLIENT_ID", null)
        Assert.assertTrue(testLoginCallbackV2.verifyOnError(grabIdPartnerError))
        testLoginCallbackV2.verifyOnSuccess(0)
    }

    @Test
    fun `verify login with no cache and discovery endpoints`() {
        prerequisiteToValidateCallDiscovery(FAKE_AUTH_ENDPOINT, FAKE_TOKEN_ENDPOINT, FAKE_ID_TOKEN_ENDPOINT, FAKE_CLIENT_PUBLIC_INFO_ENDPOINT)
        verify(launchAppForAuthorization, times(1)).launchOAuthFlow(any(), any(), any(), eq(false))
    }

    @Test
    fun `verify loginV2 with no cache and discovery endpoints`() {
        prerequisiteToValidateCallDiscovery(FAKE_AUTH_ENDPOINT, FAKE_TOKEN_ENDPOINT, FAKE_ID_TOKEN_ENDPOINT, FAKE_CLIENT_PUBLIC_INFO_ENDPOINT, isV2Api = true)
        verify(launchAppForAuthorization, times(1)).launchOAuthFlow(any(), any(), any(), eq(false))
    }

    @Test
    fun `verify login with with login_hint in the loginSession object`() {
        prerequisiteToValidateCallDiscovery(auth_endpoint = FAKE_AUTH_ENDPOINT, token_endpoint = FAKE_TOKEN_ENDPOINT, id_token_verification_endpoint = FAKE_ID_TOKEN_ENDPOINT, client_public_info_endpoint = FAKE_CLIENT_PUBLIC_INFO_ENDPOINT, protocolInfo = ProtocolInfo("", "", ""), loginHint = LOGIN_HINT)
        verify(launchAppForAuthorization, times(1)).launchOAuthFlow(any(), any(), any(), eq(false))
        verify(launchAppForAuthorization, times(0)).launchOAuthFlow(any(), any(), any(), eq(true))
    }

    @Test
    fun `verify loginV2 with with login_hint in the loginSession object`() {
        prerequisiteToValidateCallDiscovery(auth_endpoint = FAKE_AUTH_ENDPOINT, token_endpoint = FAKE_TOKEN_ENDPOINT, id_token_verification_endpoint = FAKE_ID_TOKEN_ENDPOINT, client_public_info_endpoint = FAKE_CLIENT_PUBLIC_INFO_ENDPOINT, protocolInfo = ProtocolInfo("", "", ""), loginHint = LOGIN_HINT, isV2Api = true)
        verify(launchAppForAuthorization, times(1)).launchOAuthFlow(any(), any(), any(), eq(false))
        verify(launchAppForAuthorization, times(0)).launchOAuthFlow(any(), any(), any(), eq(true))
    }

    @Test
    fun `verify login with with id_token_hint in the loginSession object`() {
        prerequisiteToValidateCallDiscovery(auth_endpoint = FAKE_AUTH_ENDPOINT, token_endpoint = FAKE_TOKEN_ENDPOINT, id_token_verification_endpoint = FAKE_ID_TOKEN_ENDPOINT, client_public_info_endpoint = FAKE_CLIENT_PUBLIC_INFO_ENDPOINT, protocolInfo = ProtocolInfo("", "", ""), idTokenHint = ID_TOKEN_HINT)
        verify(launchAppForAuthorization, times(1)).launchOAuthFlow(any(), any(), any(), eq(false))
        verify(launchAppForAuthorization, times(0)).launchOAuthFlow(any(), any(), any(), eq(true))
    }

    @Test
    fun `verify loginV2 with with id_token_hint in the loginSession object`() {
        prerequisiteToValidateCallDiscovery(auth_endpoint = FAKE_AUTH_ENDPOINT, token_endpoint = FAKE_TOKEN_ENDPOINT, id_token_verification_endpoint = FAKE_ID_TOKEN_ENDPOINT, client_public_info_endpoint = FAKE_CLIENT_PUBLIC_INFO_ENDPOINT, protocolInfo = ProtocolInfo("", "", ""), idTokenHint = ID_TOKEN_HINT, isV2Api = true)
        verify(launchAppForAuthorization, times(1)).launchOAuthFlow(any(), any(), any(), eq(false))
        verify(launchAppForAuthorization, times(0)).launchOAuthFlow(any(), any(), any(), eq(true))
    }

    @Test
    fun `verify login when there is a native app available`() {
        prerequisiteToValidateCallDiscovery(auth_endpoint = FAKE_AUTH_ENDPOINT, token_endpoint = FAKE_TOKEN_ENDPOINT, id_token_verification_endpoint = FAKE_ID_TOKEN_ENDPOINT, client_public_info_endpoint = FAKE_CLIENT_PUBLIC_INFO_ENDPOINT, protocolInfo = ProtocolInfo("", "", ""))
        verify(launchAppForAuthorization, times(1)).launchOAuthFlow(any(), any(), any(), eq(true))
        verify(launchAppForAuthorization, times(0)).launchOAuthFlow(any(), any(), any(), eq(false))
    }

    @Test
    fun `verify loginV2 when there is a native app available`() {
        prerequisiteToValidateCallDiscovery(auth_endpoint = FAKE_AUTH_ENDPOINT, token_endpoint = FAKE_TOKEN_ENDPOINT, id_token_verification_endpoint = FAKE_ID_TOKEN_ENDPOINT, client_public_info_endpoint = FAKE_CLIENT_PUBLIC_INFO_ENDPOINT, protocolInfo = ProtocolInfo("", "", ""), isV2Api = true)
        verify(launchAppForAuthorization, times(1)).launchOAuthFlow(any(), any(), any(), eq(true))
        verify(launchAppForAuthorization, times(0)).launchOAuthFlow(any(), any(), any(), eq(false))
    }

    @Test
    fun `verify login with no cache and cause callDiscovery exception`() {
        loginSession.clientId = FAKE_CLIENT_ID
        loginSession.redirectUri = FAKE_REDIRECT_URI
        loginSession.scope = PARTNER_SCOPE
        loginSession.serviceDiscoveryUrl = FAKE_DISCOVERY_URL
        utility.setObjectToSharedPref(null)
        grabAuthRepository.setCallDiscovery(Observable.error(Exception()))
        grabAuthRepository.setFetchClientPublicInfo(Observable.just(ClientPublicInfo(listOf(), "", "", "", "", "")))

        grabIdPartner.login(loginSession, context, testLoginCallback)
        val grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.SERVICEDISCOVERY, GrabIdPartnerErrorCode.errorInDiscoveryEndpoint, "", Exception())
        Assert.assertTrue(testLoginCallback.verifyOnError(grabIdPartnerError))
        testLoginCallback.verifyOnSuccess(0)
    }

    @Test
    fun `verify exchangeToken with empty redirectUrl`() {
        grabIdPartner.exchangeToken(loginSession, "", testLoginCallback)

        val grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.EXCHANGETOKEN, GrabIdPartnerErrorCode.invalidRedirectURI, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(testLoginCallback.verifyOnError(grabIdPartnerError))
        testLoginCallback.verifyOnSuccess(0)
    }

    @Test
    fun `verify exchangeToken with empty code in redirectUrl`() {
        prerequisiteToValidateExchangeToken("", TEST_RESPONSE_STATE, "", "", "", "", "")

        val grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.EXCHANGETOKEN, GrabIdPartnerErrorCode.invalidCode, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(testLoginCallback.verifyOnError(grabIdPartnerError))
        testLoginCallback.verifyOnSuccess(0)
    }

    @Test
    fun `verify exchangeToken with state mismatch`() {
        prerequisiteToValidateExchangeToken(TEST_RESPONSE_TYPE, TEST_RESPONSE_STATE, "", "", "", "", "")

        val grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.EXCHANGETOKEN, GrabIdPartnerErrorCode.stateMismatch, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(testLoginCallback.verifyOnError(grabIdPartnerError))
        testLoginCallback.verifyOnSuccess(0)
    }

    @Test
    fun `verify exchangeToken with missing tokenEndPoint`() {
        prerequisiteToValidateExchangeToken(TEST_RESPONSE_TYPE, TEST_RESPONSE_STATE, TEST_RESPONSE_STATE, "", "", "", "")

        val grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.EXCHANGETOKEN, GrabIdPartnerErrorCode.errorInLoginSessionObject, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(testLoginCallback.verifyOnError(grabIdPartnerError))
        testLoginCallback.verifyOnSuccess(0)
    }

    @Test
    fun `verify exchangeToken with getToken api return without access_token`() {
        prerequisiteToValidateExchangeToken(TEST_RESPONSE_TYPE, TEST_RESPONSE_STATE, TEST_RESPONSE_STATE, FAKE_TOKEN_ENDPOINT, "", "", "")

        val grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.EXCHANGETOKEN, GrabIdPartnerErrorCode.missingAccessToken, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(testLoginCallback.verifyOnError(grabIdPartnerError))
        testLoginCallback.verifyOnSuccess(0)
    }

    @Test
    fun `verify exchangeToken with getToken api return without id_token`() {
        prerequisiteToValidateExchangeToken(TEST_RESPONSE_TYPE, TEST_RESPONSE_STATE, TEST_RESPONSE_STATE, FAKE_TOKEN_ENDPOINT, TEST_ACCESS_TOKEN, "", TEST_EXPIRY_TIME)

        testLoginCallback.verifyOnSuccess(1)
    }

    @Test
    fun `verify exchangeToken with getToken api return without expiresIn`() {
        prerequisiteToValidateExchangeToken(TEST_RESPONSE_TYPE, TEST_RESPONSE_STATE, TEST_RESPONSE_STATE, FAKE_TOKEN_ENDPOINT, TEST_ACCESS_TOKEN, TEST_ID_TOKEN, "")

        val grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.EXCHANGETOKEN, GrabIdPartnerErrorCode.missingAccessTokenExpiry, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(testLoginCallback.verifyOnError(grabIdPartnerError))
        testLoginCallback.verifyOnSuccess(0)
    }

    @Test
    fun `verify exchangeToken with getToken api returns all mandatory parameters`() {
        prerequisiteToValidateExchangeToken(TEST_RESPONSE_TYPE, TEST_RESPONSE_STATE, TEST_RESPONSE_STATE, FAKE_TOKEN_ENDPOINT, TEST_ACCESS_TOKEN, TEST_ID_TOKEN, TEST_EXPIRY_TIME)
        testLoginCallback.verifyOnSuccess(1)
    }

    @Test
    fun `verify exchangeToken with getToken api returns exception`() {
        val getURLParamMap = HashMap<String, String>()
        getURLParamMap[GrabIdPartner.RESPONSE_TYPE] = TEST_RESPONSE_TYPE
        getURLParamMap[GrabIdPartner.RESPONSE_STATE] = TEST_RESPONSE_STATE
        utility.setURLParamReturn(getURLParamMap)
        // set the state and token endpoint in the loginSession object
        loginSession.stateInternal = TEST_RESPONSE_STATE
        loginSession.tokenEndpoint = FAKE_TOKEN_ENDPOINT

        grabAuthRepository.setGetToken(Observable.error(Exception()))

        grabIdPartner.exchangeToken(loginSession, FAKE_REDIRECT_URI, testLoginCallback)
        val grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.EXCHANGETOKEN, GrabIdPartnerErrorCode.network, "", null)
        Assert.assertTrue(testLoginCallback.verifyOnError(grabIdPartnerError))
        testLoginCallback.verifyOnSuccess(0)
    }

    @Test
    fun `verify getIdTokenInfo with idTokenVerificationEndpoint as empty`() {
        // set the idTokenVerificationEndpoint endpoint in the loginSession object
        loginSession.idTokenVerificationEndpoint = ""

        grabIdPartner.getIdTokenInfo(loginSession, testGetIdTokenInfoCallback)
        val grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.GETIDTOKENINFO, GrabIdPartnerErrorCode.errorInLoginSessionObject, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(testGetIdTokenInfoCallback.verifyOnError(grabIdPartnerError))
        testGetIdTokenInfoCallback.verifyOnSuccess(0)
    }

    @Test
    fun `verify getIdTokenInfo with api doesn't returns same nonce`() {
        // set the idTokenVerificationEndpoint endpoint in the loginSession object
        loginSession.idTokenVerificationEndpoint = FAKE_ID_TOKEN_ENDPOINT

        val idTokenInfo = IdTokenInfo()
        idTokenInfo.nonceInternal = TEST_NONCE
        grabAuthRepository.setGetIdTokenInfo(Observable.just(idTokenInfo))
        grabIdPartner.getIdTokenInfo(loginSession, testGetIdTokenInfoCallback)

        val grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.GETIDTOKENINFO, GrabIdPartnerErrorCode.errorInGetIdTokenInfo, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(testGetIdTokenInfoCallback.verifyOnError(grabIdPartnerError))
        testGetIdTokenInfoCallback.verifyOnSuccess(0)
    }

    @Test
    fun `verify getIdTokenInfo with getIdTokenInfo api return success callback`() {
        // set the idTokenVerificationEndpoint endpoint in the loginSession object
        loginSession.idTokenVerificationEndpoint = FAKE_ID_TOKEN_ENDPOINT

        val idTokenInfo = IdTokenInfo()
        grabAuthRepository.setGetIdTokenInfo(Observable.just(idTokenInfo))
        grabIdPartner.getIdTokenInfo(loginSession, testGetIdTokenInfoCallback)

        testGetIdTokenInfoCallback.verifyOnSuccess(1)
    }

    @Test
    fun `verify getIdTokenInfo caching scenario with a valid idToken object in cache`() {
        // set the idTokenVerificationEndpoint endpoint and nonceInternal in the loginSession object
        loginSession.idTokenVerificationEndpoint = FAKE_ID_TOKEN_ENDPOINT
        loginSession.nonceInternal = TEST_NONCE

        // create non-expired IdTokenInfo object and set it in the cache
        val idTokenInfo = createValidIdTokenInfo()
        utility.setSerializedIdTokenInfo(idTokenInfo)

        // invoke the getIdTokenInfo api with a new IdTokenInfo object instance but make sure getIdTokenInfo returns the cached version of IdTokenInfo instance
        val idTokenInfo2 = createValidIdTokenInfo()
        grabAuthRepository.setGetIdTokenInfo(Observable.just(idTokenInfo2))
        grabIdPartner.getIdTokenInfo(loginSession, testGetIdTokenInfoCallback)

        testGetIdTokenInfoCallback.verifyOnSuccess(1)
        testGetIdTokenInfoCallback.verifyOnSuccess(idTokenInfo)
    }

    @Test
    fun `verify getIdTokenInfo caching scenario when idToken has expired`() {
        // set the idTokenVerificationEndpoint endpoint and nonceInternal in the loginSession object
        loginSession.idTokenVerificationEndpoint = FAKE_ID_TOKEN_ENDPOINT
        loginSession.nonceInternal = TEST_NONCE

        // create expired IdTokenInfo object
        utility.setSerializedIdTokenInfo(createExpiredIdTokenInfo())

        // create a new non-expired IdTokenInfo instance and verify getIdTokenInfo api is returning this instance
        val idTokenInfo2 = createValidIdTokenInfo()
        grabAuthRepository.setGetIdTokenInfo(Observable.just(idTokenInfo2))
        grabIdPartner.getIdTokenInfo(loginSession, testGetIdTokenInfoCallback)

        testGetIdTokenInfoCallback.verifyOnSuccess(1)
        testGetIdTokenInfoCallback.verifyOnSuccess(idTokenInfo2)
    }

    @Test
    fun `verify teardown`() {
        grabIdPartner.teardown()
        verify(compositeDisposable, times(1)).clear()
    }

    @Test
    fun `verify logout`() {
        grabIdPartner.logout(loginSession, testLoginCallback)
        testLoginCallback.verifyOnSuccess(1)
    }

    private fun prerequisiteToValidateLoadLoginSessionWithDifferentPartnerinfo(client_id: String?, redirect_uri: String?, discovery_url: String?, partner_scope: String?) {
        GrabIdPartner.isSdkInitialized = true

        val partnerInfoMap = HashMap<String, String?>()
        partnerInfoMap[PARTNER_CLIENT_ID_ATTRIBUTE] = client_id
        partnerInfoMap[PARTNER_REDIRECT_URI_ATTRIBUTE] = redirect_uri
        partnerInfoMap[PARTNER_SERVICE_DISCOVERY_URL] = discovery_url
        partnerInfoMap[PARTNER_SCOPE_ATTRIBUTE] = partner_scope

        // save the partnerInfoMap map in the stub utility
        utility.setPartnerInfo(partnerInfoMap)
        grabIdPartner.loadLoginSession(loginSessionCallback)
    }

    private fun prerequisiteToValidateLogin(client_id: String, redirect_url: String, partner_scope: String, serviceDiscovery_url: String, isV2Api: Boolean = false) {
        loginSession.clientId = client_id
        loginSession.redirectUri = redirect_url
        loginSession.scope = partner_scope
        loginSession.serviceDiscoveryUrl = serviceDiscovery_url
        if (isV2Api) {
            grabIdPartner.loginV2(loginSession, activity, testLoginCallbackV2)
        } else {
            grabIdPartner.login(loginSession, context, testLoginCallback)
        }
    }

    private fun prerequisiteToValidateCallDiscovery(auth_endpoint: String, token_endpoint: String, id_token_verification_endpoint: String, client_public_info_endpoint: String, protocolInfo: ProtocolInfo? = null, loginHint: String? = null, idTokenHint: String? = null, isV2Api: Boolean = false) {
        loginSession.clientId = FAKE_CLIENT_ID
        loginSession.redirectUri = FAKE_REDIRECT_URI
        loginSession.scope = PARTNER_SCOPE
        loginSession.serviceDiscoveryUrl = FAKE_DISCOVERY_URL
        loginSession.loginHint = loginHint
        loginSession.idTokenHint = idTokenHint
        utility.setObjectToSharedPref(null)

        grabAuthRepository.setCallDiscovery(Observable.just(DiscoveryResponse(auth_endpoint, token_endpoint, id_token_verification_endpoint, client_public_info_endpoint)))
        grabAuthRepository.setFetchClientPublicInfo(Observable.just(ClientPublicInfo(listOf(), "", "", "", "", "")))
        utility.setIsPackageInstalled(protocolInfo)

        if (isV2Api) {
            whenever(activity.packageManager).thenReturn(packageManager)
            grabIdPartner.loginV2(loginSession, activity, testLoginCallbackV2)
        } else {
            whenever(context.packageManager).thenReturn(packageManager)
            grabIdPartner.login(loginSession, context, testLoginCallback)
        }
    }

    private fun prerequisiteToValidateExchangeToken(response_type: String, state: String, loginSession_state: String, token_endpoint: String, access_token: String?, id_token: String?, expires_in: String?) {
        val getURLParamMap = HashMap<String, String>()
        getURLParamMap[GrabIdPartner.RESPONSE_TYPE] = response_type
        getURLParamMap[GrabIdPartner.RESPONSE_STATE] = state
        utility.setURLParamReturn(getURLParamMap)

        // set the state and token endpoint in the loginSession object
        loginSession.stateInternal = loginSession_state
        loginSession.tokenEndpoint = token_endpoint
        grabAuthRepository.setGetToken(Observable.just(TokenAPIResponse(access_token = access_token, id_token = id_token, expires_in = expires_in)))

        grabIdPartner.exchangeToken(loginSession, FAKE_REDIRECT_URI, testLoginCallback)
    }

    private fun createExpiredIdTokenInfo(): IdTokenInfo {
        val idTokenInfo = IdTokenInfo()
        idTokenInfo.audienceInternal = UUID.randomUUID().toString()
        idTokenInfo.nonceInternal = UUID.randomUUID().toString()
        idTokenInfo.expirationInternal = utility.subtractDaysFromCurrentDate(3)
        idTokenInfo.notValidBeforeInternal = utility.subtractDaysFromCurrentDate(5)
        return idTokenInfo
    }

    private fun createValidIdTokenInfo(): IdTokenInfo {
        val idTokenInfo = IdTokenInfo()
        idTokenInfo.audienceInternal = UUID.randomUUID().toString()
        idTokenInfo.nonceInternal = TEST_NONCE
        idTokenInfo.expirationInternal = utility.addDaysToCurrentDate(3)
        idTokenInfo.notValidBeforeInternal = utility.getCurrentTimeInUTC()

        return idTokenInfo
    }
}

class TestLoginCallback : LoginCallback, ExchangeTokenCallback, LogoutCallback {
    private val mockLoginCallback: LoginCallback = mock(LoginCallback::class.java)
    private var error: GrabIdPartnerError? = null

    override fun onSuccess() {
        mockLoginCallback.onSuccess()
    }

    override fun onError(grabIdPartnerError: GrabIdPartnerError) {
        this.error = grabIdPartnerError
    }

    fun verifyOnError(expected: GrabIdPartnerError): Boolean {
        return this.error != null && this.error?.localizeMessage?.trim().equals(expected.localizeMessage?.trim()) &&
                this.error?.code?.equals(expected.code) ?: false &&
                this.error?.grabIdPartnerErrorDomain?.equals(expected.grabIdPartnerErrorDomain) ?: false
    }

    fun verifyOnSuccess(i: Int) {
        Mockito.verify(mockLoginCallback, Times(i)).onSuccess()
    }

    fun reset() {
        Mockito.reset(mockLoginCallback)
    }
}

class TestLoginCallbackV2 : LoginCallbackV2 {
    private val mockLoginCallbackV2: LoginCallbackV2 = mock()
    private var error: GrabIdPartnerError? = null
    override fun onSuccessCache() {
        mockLoginCallbackV2.onSuccessCache()
    }

    override fun onSuccess() {
        mockLoginCallbackV2.onSuccess()
    }

    override fun onError(grabIdPartnerError: GrabIdPartnerError) {
        this.error = grabIdPartnerError
    }

    fun verifyOnError(expected: GrabIdPartnerError): Boolean {
        return this.error != null && this.error?.localizeMessage?.trim().equals(expected.localizeMessage?.trim()) &&
                this.error?.code?.equals(expected.code) ?: false &&
                this.error?.grabIdPartnerErrorDomain?.equals(expected.grabIdPartnerErrorDomain) ?: false
    }

    fun verifyOnSuccess(i: Int) {
        Mockito.verify(mockLoginCallbackV2, Times(i)).onSuccess()
    }
}

class TestLoginSessionCallback : LoginSessionCallback {
    private val mockLoginSessionCallback: LoginSessionCallback = Mockito.mock(LoginSessionCallback::class.java)
    private var error: GrabIdPartnerError? = null
    private var loginSession: LoginSession? = null

    override fun onSuccess(loginSession: LoginSession) {
        this.mockLoginSessionCallback.onSuccess(loginSession)
        this.loginSession = loginSession
    }

    override fun onError(grabIdPartnerError: GrabIdPartnerError) {
        this.error = grabIdPartnerError
    }

    fun verifyOnSuccess(i: Int) {
        Mockito.verify(mockLoginSessionCallback, Times(i)).onSuccess(any())
    }

    fun verifyAllRequiredParametersExists() {
        Assert.assertTrue(!this.loginSession?.codeVerifier.isNullOrBlank())
        Assert.assertTrue(!this.loginSession?.state.isNullOrBlank())
        Assert.assertTrue(!this.loginSession?.nonce.isNullOrBlank())
        Assert.assertTrue(!this.loginSession?.codeChallenge.isNullOrBlank())
    }

    fun verifyOnError(expected: GrabIdPartnerError): Boolean {
        return this.error != null && this.error?.localizeMessage.equals(expected.localizeMessage) &&
                this.error?.code?.equals(expected.code) ?: false &&
                this.error?.grabIdPartnerErrorDomain?.equals(expected.grabIdPartnerErrorDomain) ?: false
    }

    fun reset() {
        Mockito.reset(mockLoginSessionCallback)
    }
}

class TestGetIdTokenInfoCallback : GetIdTokenInfoCallback {
    private val mockGetIdTokenInfoCallback: GetIdTokenInfoCallback = Mockito.mock(GetIdTokenInfoCallback::class.java)
    private var error: GrabIdPartnerError? = null
    private var grabIdTokenInfo: IdTokenInfo? = null

    override fun onSuccess(idTokenInfo: IdTokenInfo) {
        mockGetIdTokenInfoCallback.onSuccess(idTokenInfo)
        this.grabIdTokenInfo = idTokenInfo
    }

    override fun onError(grabIdPartnerError: GrabIdPartnerError) {
        this.error = grabIdPartnerError
    }

    fun verifyOnError(expected: GrabIdPartnerError): Boolean {
        return this.error != null && this.error?.localizeMessage.equals(expected.localizeMessage) &&
                this.error?.code?.equals(expected.code) ?: false &&
                this.error?.grabIdPartnerErrorDomain?.equals(expected.grabIdPartnerErrorDomain) ?: false
    }

    fun verifyOnSuccess(i: Int) {
        Mockito.verify(mockGetIdTokenInfoCallback, Times(i)).onSuccess(any())
    }

    fun verifyOnSuccess(idTokenInfo: IdTokenInfo) {
        Assert.assertTrue(this.grabIdTokenInfo?.audience == idTokenInfo.audience &&
                this.grabIdTokenInfo?.service == idTokenInfo.service &&
                this.grabIdTokenInfo?.nonce == idTokenInfo.nonce &&
                this.grabIdTokenInfo?.expiration == idTokenInfo.expiration &&
                this.grabIdTokenInfo?.issueDate == idTokenInfo.issueDate &&
                this.grabIdTokenInfo?.issuer == idTokenInfo.issuer &&
                this.grabIdTokenInfo?.tokenId == idTokenInfo.tokenId &&
                this.grabIdTokenInfo?.partnerId == idTokenInfo.partnerId &&
                this.grabIdTokenInfo?.partnerUserId == idTokenInfo.partnerUserId &&
                this.grabIdTokenInfo?.notValidBefore == idTokenInfo.notValidBefore)
    }
}