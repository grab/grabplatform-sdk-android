/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import com.grab.partner.sdk.models.DiscoveryResponse
import com.grab.partner.sdk.models.GrabIdPartnerError
import com.grab.partner.sdk.models.GrabIdPartnerErrorCode
import com.grab.partner.sdk.models.GrabIdPartnerErrorDomain
import com.grab.partner.sdk.models.IdTokenInfo
import com.grab.partner.sdk.models.LoginSession
import com.grab.partner.sdk.models.TokenAPIResponse
import com.grab.partner.sdk.scheduleprovider.TestSchedulerProvider
import com.grab.partner.sdk.utils.Utility
import com.nhaarman.mockitokotlin2.any
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.internal.verification.Times
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import java.security.KeyPair
import java.security.KeyStore
import java.util.UUID

@RunWith(PowerMockRunner::class)
@PrepareForTest(Base64::class, LoginSession::class, Utility::class, KeyStore::class, Log::class)
class GrabIdPartnerTest {
    private var grabIdPartner = GrabIdPartner.instance
    private var loginSessionCallback = TestLoginSessionCallback()
    private var testLoginCallback = TestLoginCallback()
    private var testGetIdTokenInfoCallback = TestGetIdTokenInfoCallback()
    private var loginSession = LoginSession()
    private var utility = StubUtility()
    private var context: Context = Mockito.mock(Context::class.java)
    private var sharedPreferences = Mockito.mock(SharedPreferences::class.java)
    private var grabAuthRepository = StubGrabAuthRepository()
    private var scheduleProvider = TestSchedulerProvider()
    private var chromeCustomTab = StubChromeCustomTab()
    private var keyPair = PowerMockito.mock(KeyPair::class.java)
    private var androidKeyStoreWrapper = StubAndroidKeyStoreWrapper(keyPair)
    private var cipherWrapper = StubCipherWrapper()
    private var compositeDisposable = CompositeDisposable()

    private val FAKE_CLIENT_ID = "fake_client_id"
    private val FAKE_REDIRECT_URI = "fake_redirectUri"
    private val FAKE_DISCOVERY_URL = "fake_discovery_url"
    private val FAKE_AUTH_ENDPOINT = "fake_auth_endpoint"
    private val FAKE_TOKEN_ENDPOINT = "fake_token_endpoint"
    private val FAKE_ID_TOKEN_ENDPOINT = "fake_id_token_verification_endpoint"
    private val PARTNER_SCOPE = "scope"
    private val TEST_RESPONSE_TYPE = "code"
    private val TEST_RESPONSE_STATE = "state"
    private val TEST_ACCESS_TOKEN = "access_token"
    private val TEST_ID_TOKEN = "id_token"
    private val TEST_NONCE = "nonce"
    private val TEST_EXPIRY_TIME = "100"

    @Before
    fun setUp() {
        PowerMockito.mockStatic(Log::class.java)
        testLoginCallback.reset()
        loginSessionCallback.reset()
        grabIdPartner.utility = utility
        grabIdPartner.grabAuthRepository = grabAuthRepository
        grabIdPartner.schedulerProvider = scheduleProvider
        grabIdPartner.sharedPreferences = sharedPreferences
        grabIdPartner.chromeCustomTab = chromeCustomTab
        grabIdPartner.androidKeyStoreWrapper = androidKeyStoreWrapper
        grabIdPartner.cipherWrapper = cipherWrapper
        grabIdPartner.compositeDisposable = compositeDisposable
    }

    @Test
    fun `verify loadLoginSession without initializing the SDK`() {
        GrabIdPartner.isSdkInitialized = false
        grabIdPartner.loadLoginSession(loginSessionCallback)

        var grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.LOADLOGINSESSION, GrabIdPartnerErrorCode.sdkNotInitialized, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(loginSessionCallback.verifyOnError(grabIdPartnerError))
    }

    @Test
    fun `verify loadLoginSession with null clientId`() {
        prerequisiteToValidateLoadLoginSessionWithDifferentPartnerinfo(null, null, null, null)
        var grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.LOADLOGINSESSION, GrabIdPartnerErrorCode.invalidClientId, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(loginSessionCallback.verifyOnError(grabIdPartnerError))
    }

    @Test
    fun `verify loadLoginSession with empty clientId`() {
        prerequisiteToValidateLoadLoginSessionWithDifferentPartnerinfo("", null, null, null)
        var grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.LOADLOGINSESSION, GrabIdPartnerErrorCode.invalidClientId, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(loginSessionCallback.verifyOnError(grabIdPartnerError))
    }

    @Test
    fun `verify loadLoginSession with null redirectUri`() {
        prerequisiteToValidateLoadLoginSessionWithDifferentPartnerinfo(FAKE_CLIENT_ID, null, null, null)
        var grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.LOADLOGINSESSION, GrabIdPartnerErrorCode.invalidRedirectURI, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(loginSessionCallback.verifyOnError(grabIdPartnerError))
    }

    @Test
    fun `verify loadLoginSession with empty redirectUri`() {
        prerequisiteToValidateLoadLoginSessionWithDifferentPartnerinfo(FAKE_CLIENT_ID, "", null, null)
        var grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.LOADLOGINSESSION, GrabIdPartnerErrorCode.invalidRedirectURI, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(loginSessionCallback.verifyOnError(grabIdPartnerError))
    }

    @Test
    fun `verify loadLoginSession with null serviceDiscoveryUrl`() {
        prerequisiteToValidateLoadLoginSessionWithDifferentPartnerinfo(FAKE_CLIENT_ID, FAKE_REDIRECT_URI, null, null)
        var grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.LOADLOGINSESSION, GrabIdPartnerErrorCode.invalidDiscoveryEndpoint, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(loginSessionCallback.verifyOnError(grabIdPartnerError))
    }

    @Test
    fun `verify loadLoginSession with empty serviceDiscoveryUrl`() {
        prerequisiteToValidateLoadLoginSessionWithDifferentPartnerinfo(FAKE_CLIENT_ID, FAKE_REDIRECT_URI, "", null)
        var grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.LOADLOGINSESSION, GrabIdPartnerErrorCode.invalidDiscoveryEndpoint, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(loginSessionCallback.verifyOnError(grabIdPartnerError))
    }

    @Test
    fun `verify loadLoginSession with null scope`() {
        prerequisiteToValidateLoadLoginSessionWithDifferentPartnerinfo(FAKE_CLIENT_ID, FAKE_REDIRECT_URI, FAKE_DISCOVERY_URL, null)
        var grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.LOADLOGINSESSION, GrabIdPartnerErrorCode.invalidPartnerScope, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(loginSessionCallback.verifyOnError(grabIdPartnerError))
    }

    @Test
    fun `verify loadLoginSession with empty scope`() {
        prerequisiteToValidateLoadLoginSessionWithDifferentPartnerinfo(FAKE_CLIENT_ID, FAKE_REDIRECT_URI, FAKE_DISCOVERY_URL, "")
        var grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.LOADLOGINSESSION, GrabIdPartnerErrorCode.invalidPartnerScope, CONST_READ_RESOURCE_STRING, null)
        loginSessionCallback.verifyOnSuccess(0)
        Assert.assertTrue(loginSessionCallback.verifyOnError(grabIdPartnerError))
    }

    @Test
    fun `verify loadLoginSession with required partner info`() {
        prerequisiteToValidateLoadLoginSessionWithDifferentPartnerinfo(FAKE_CLIENT_ID, FAKE_REDIRECT_URI, FAKE_DISCOVERY_URL, PARTNER_SCOPE)
        loginSessionCallback.verifyOnSuccess(1)
    }

    @Test
    fun `verify loginSession has required parameters after loadLoginSession`() {
        prerequisiteToValidateLoadLoginSessionWithDifferentPartnerinfo(FAKE_CLIENT_ID, FAKE_REDIRECT_URI, FAKE_DISCOVERY_URL, PARTNER_SCOPE)
        loginSessionCallback.verifyOnSuccess(1)
        loginSessionCallback.verifyAllRequiredParametersExists()
    }

    @Test
    fun `verify login with empty client id`() {
        prerequisiteToValidateLogin("", "", "", "")

        var grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.LOGIN, GrabIdPartnerErrorCode.invalidClientId, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(testLoginCallback.verifyOnError(grabIdPartnerError))
        testLoginCallback.verifyOnSuccess(0)
    }

    @Test
    fun `verify login with empty redirectUri`() {
        prerequisiteToValidateLogin(FAKE_CLIENT_ID, "", "", "")

        var grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.LOGIN, GrabIdPartnerErrorCode.invalidRedirectURI, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(testLoginCallback.verifyOnError(grabIdPartnerError))
        testLoginCallback.verifyOnSuccess(0)
    }

    @Test
    fun `verify login with empty scope`() {
        prerequisiteToValidateLogin(FAKE_CLIENT_ID, FAKE_REDIRECT_URI, "", "")

        var grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.LOGIN, GrabIdPartnerErrorCode.invalidPartnerScope, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(testLoginCallback.verifyOnError(grabIdPartnerError))
        testLoginCallback.verifyOnSuccess(0)
    }

    @Test
    fun `verify login with empty serviceDiscoveryUrl`() {
        prerequisiteToValidateLogin(FAKE_CLIENT_ID, FAKE_REDIRECT_URI, PARTNER_SCOPE, "")

        var grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.LOGIN, GrabIdPartnerErrorCode.invalidDiscoveryEndpoint, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(testLoginCallback.verifyOnError(grabIdPartnerError))
        testLoginCallback.verifyOnSuccess(0)

    }

    @Test
    fun `verify login with no cache and empty authorization_endpoint`() {
        prerequisiteToValidateCallDiscovery("", "", "")

        var grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.SERVICEDISCOVERY, GrabIdPartnerErrorCode.errorInDiscoveryEndpoint, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(testLoginCallback.verifyOnError(grabIdPartnerError))
        testLoginCallback.verifyOnSuccess(0)
    }

    @Test
    fun `verify login with no cache and empty token_endpoint`() {
        prerequisiteToValidateCallDiscovery(FAKE_AUTH_ENDPOINT, "", "")

        var grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.SERVICEDISCOVERY, GrabIdPartnerErrorCode.errorInDiscoveryEndpoint, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(testLoginCallback.verifyOnError(grabIdPartnerError))
        testLoginCallback.verifyOnSuccess(0)
    }

    @Test
    fun `verify login with no cache and empty id_token_verification_endpoint`() {
        prerequisiteToValidateCallDiscovery(FAKE_AUTH_ENDPOINT, FAKE_TOKEN_ENDPOINT, "")
        var grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.SERVICEDISCOVERY, GrabIdPartnerErrorCode.errorInDiscoveryEndpoint, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(testLoginCallback.verifyOnError(grabIdPartnerError))
        testLoginCallback.verifyOnSuccess(0)
    }

    @Test
    fun `verify login with no cache and discovery endpoints`() {
        prerequisiteToValidateCallDiscovery(FAKE_AUTH_ENDPOINT, FAKE_TOKEN_ENDPOINT, FAKE_ID_TOKEN_ENDPOINT)
        testLoginCallback.verifyOnSuccess(1)
    }

    @Test
    fun `verify login with no cache and cause callDiscovery exception`() {
        loginSession.clientId = FAKE_CLIENT_ID
        loginSession.redirectUri = FAKE_REDIRECT_URI
        loginSession.scope = PARTNER_SCOPE
        loginSession.serviceDiscoveryUrl = FAKE_DISCOVERY_URL
        utility.setObjectToSharedPref(null)
        grabAuthRepository.setCallDiscovery(Observable.error(Exception()))

        grabIdPartner.login(loginSession, context, testLoginCallback)
        var grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.SERVICEDISCOVERY, GrabIdPartnerErrorCode.errorInDiscoveryEndpoint, "", Exception())
        Assert.assertTrue(testLoginCallback.verifyOnError(grabIdPartnerError))
        testLoginCallback.verifyOnSuccess(0)
    }

    @Test
    fun `verify exchangeToken with empty redirectUrl`() {
        grabIdPartner.exchangeToken(loginSession, "", testLoginCallback)

        var grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.EXCHANGETOKEN, GrabIdPartnerErrorCode.invalidRedirectURI, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(testLoginCallback.verifyOnError(grabIdPartnerError))
        testLoginCallback.verifyOnSuccess(0)
    }

    @Test
    fun `verify exchangeToken with empty code in redirectUrl`() {
        prerequisiteToValidateExchangeToken("", TEST_RESPONSE_STATE, "", "", "", "", "")

        var grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.EXCHANGETOKEN, GrabIdPartnerErrorCode.invalidCode, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(testLoginCallback.verifyOnError(grabIdPartnerError))
        testLoginCallback.verifyOnSuccess(0)
    }

    @Test
    fun `verify exchangeToken with state mismatch`() {
        prerequisiteToValidateExchangeToken(TEST_RESPONSE_TYPE, TEST_RESPONSE_STATE, "", "", "", "", "")

        var grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.EXCHANGETOKEN, GrabIdPartnerErrorCode.stateMismatch, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(testLoginCallback.verifyOnError(grabIdPartnerError))
        testLoginCallback.verifyOnSuccess(0)
    }

    @Test
    fun `verify exchangeToken with missing tokenEndPoint`() {
        prerequisiteToValidateExchangeToken(TEST_RESPONSE_TYPE, TEST_RESPONSE_STATE, TEST_RESPONSE_STATE, "", "", "", "")

        var grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.EXCHANGETOKEN, GrabIdPartnerErrorCode.errorInLoginSessionObject, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(testLoginCallback.verifyOnError(grabIdPartnerError))
        testLoginCallback.verifyOnSuccess(0)
    }

    @Test
    fun `verify exchangeToken with getToken api return without access_token`() {
        prerequisiteToValidateExchangeToken(TEST_RESPONSE_TYPE, TEST_RESPONSE_STATE, TEST_RESPONSE_STATE, FAKE_TOKEN_ENDPOINT, "", "", "")

        var grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.EXCHANGETOKEN, GrabIdPartnerErrorCode.missingAccessToken, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(testLoginCallback.verifyOnError(grabIdPartnerError))
        testLoginCallback.verifyOnSuccess(0)
    }

    @Test
    fun `verify exchangeToken with getToken api return without id_token`() {
        prerequisiteToValidateExchangeToken(TEST_RESPONSE_TYPE, TEST_RESPONSE_STATE, TEST_RESPONSE_STATE, FAKE_TOKEN_ENDPOINT, TEST_ACCESS_TOKEN, "", "")

        var grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.EXCHANGETOKEN, GrabIdPartnerErrorCode.missingIdToken, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(testLoginCallback.verifyOnError(grabIdPartnerError))
        testLoginCallback.verifyOnSuccess(0)
    }

    @Test
    fun `verify exchangeToken with getToken api return without expiresIn`() {
        prerequisiteToValidateExchangeToken(TEST_RESPONSE_TYPE, TEST_RESPONSE_STATE, TEST_RESPONSE_STATE, FAKE_TOKEN_ENDPOINT, TEST_ACCESS_TOKEN, TEST_ID_TOKEN, "")

        var grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.EXCHANGETOKEN, GrabIdPartnerErrorCode.missingAccessTokenExpiry, CONST_READ_RESOURCE_STRING, null)
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
        var getURLParamMap = HashMap<String, String>()
        getURLParamMap[GrabIdPartner.RESPONSE_TYPE] = TEST_RESPONSE_TYPE
        getURLParamMap[GrabIdPartner.RESPONSE_STATE] = TEST_RESPONSE_STATE
        utility.setURLParamReturn(getURLParamMap)
        // set the state and token endpoint in the loginSession object
        loginSession.stateInternal = TEST_RESPONSE_STATE
        loginSession.tokenEndpoint = FAKE_TOKEN_ENDPOINT

        grabAuthRepository.setGetToken(Observable.error(Exception()))

        grabIdPartner.exchangeToken(loginSession, FAKE_REDIRECT_URI, testLoginCallback)
        var grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.EXCHANGETOKEN, GrabIdPartnerErrorCode.network, "", null)
        Assert.assertTrue(testLoginCallback.verifyOnError(grabIdPartnerError))
        testLoginCallback.verifyOnSuccess(0)
    }

    @Test
    fun `verify getIdTokenInfo with idTokenVerificationEndpoint as empty`() {
        // set the idTokenVerificationEndpoint endpoint in the loginSession object
        loginSession.idTokenVerificationEndpoint = ""

        grabIdPartner.getIdTokenInfo(loginSession, testGetIdTokenInfoCallback)
        var grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.GETIDTOKENINFO, GrabIdPartnerErrorCode.errorInLoginSessionObject, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(testGetIdTokenInfoCallback.verifyOnError(grabIdPartnerError))
        testGetIdTokenInfoCallback.verifyOnSuccess(0)
    }

    @Test
    fun `verify getIdTokenInfo with api doesn't returns same nonce`() {
        // set the idTokenVerificationEndpoint endpoint in the loginSession object
        loginSession.idTokenVerificationEndpoint = FAKE_ID_TOKEN_ENDPOINT

        var idTokenInfo = IdTokenInfo()
        idTokenInfo.nonceInternal = TEST_NONCE
        grabAuthRepository.setGetIdTokenInfo(Observable.just(idTokenInfo))
        grabIdPartner.getIdTokenInfo(loginSession, testGetIdTokenInfoCallback)

        var grabIdPartnerError = GrabIdPartnerError(GrabIdPartnerErrorDomain.GETIDTOKENINFO, GrabIdPartnerErrorCode.errorInGetIdTokenInfo, CONST_READ_RESOURCE_STRING, null)
        Assert.assertTrue(testGetIdTokenInfoCallback.verifyOnError(grabIdPartnerError))
        testGetIdTokenInfoCallback.verifyOnSuccess(0)
    }

    @Test
    fun `verify getIdTokenInfo with getIdTokenInfo api return success callback`() {
        // set the idTokenVerificationEndpoint endpoint in the loginSession object
        loginSession.idTokenVerificationEndpoint = FAKE_ID_TOKEN_ENDPOINT

        var idTokenInfo = IdTokenInfo()
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
        var idTokenInfo = createValidIdTokenInfo()
        utility.setSerializedIdTokenInfo(idTokenInfo)

        // invoke the getIdTokenInfo api with a new IdTokenInfo object instance but make sure getIdTokenInfo returns the cached version of IdTokenInfo instance
        var idTokenInfo2 = createValidIdTokenInfo()
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
        var idTokenInfo2 = createValidIdTokenInfo()
        grabAuthRepository.setGetIdTokenInfo(Observable.just(idTokenInfo2))
        grabIdPartner.getIdTokenInfo(loginSession, testGetIdTokenInfoCallback)

        testGetIdTokenInfoCallback.verifyOnSuccess(1)
        testGetIdTokenInfoCallback.verifyOnSuccess(idTokenInfo2)
    }

    private fun prerequisiteToValidateLoadLoginSessionWithDifferentPartnerinfo(client_id: String?, redirect_uri: String?, discovery_url: String?, partner_scope: String?) {
        GrabIdPartner.isSdkInitialized = true

        var partnerInfoMap = HashMap<String, String?>()
        partnerInfoMap[PARTNER_CLIENT_ID_ATTRIBUTE] = client_id
        partnerInfoMap[PARTNER_REDIRECT_URI_ATTRIBUTE] = redirect_uri
        partnerInfoMap[PARTNER_SERVICE_DISCOVERY_URL] = discovery_url
        partnerInfoMap[PARTNER_SCOPE_ATTRIBUTE] = partner_scope

        // save the partnerInfoMap map in the stub utility
        utility.setPartnerInfo(partnerInfoMap)
        grabIdPartner.loadLoginSession(loginSessionCallback)
    }

    private fun prerequisiteToValidateLogin(client_id: String, redirect_url: String, partner_scope: String, serviceDiscovery_url: String) {
        loginSession.clientId = client_id
        loginSession.redirectUri = redirect_url
        loginSession.scope = partner_scope
        loginSession.serviceDiscoveryUrl = serviceDiscovery_url
        grabIdPartner.login(loginSession, context, testLoginCallback)
    }

    private fun prerequisiteToValidateCallDiscovery(auth_endpoint: String, token_endpoint: String, id_token_verification_endpoint: String) {
        loginSession.clientId = FAKE_CLIENT_ID
        loginSession.redirectUri = FAKE_REDIRECT_URI
        loginSession.scope = PARTNER_SCOPE
        loginSession.serviceDiscoveryUrl = FAKE_DISCOVERY_URL
        utility.setObjectToSharedPref(null)

        grabAuthRepository.setCallDiscovery(Observable.just(DiscoveryResponse(auth_endpoint, token_endpoint, id_token_verification_endpoint)))
        grabIdPartner.login(loginSession, context, testLoginCallback)
    }

    private fun prerequisiteToValidateExchangeToken(response_type: String, state: String, loginSession_state: String, token_endpoint: String, access_token: String?, id_token: String?, expires_in: String?) {
        var getURLParamMap = HashMap<String, String>()
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
        var idTokenInfo = IdTokenInfo()
        idTokenInfo.audienceInternal = UUID.randomUUID().toString()
        idTokenInfo.nonceInternal = UUID.randomUUID().toString()
        idTokenInfo.expirationInternal = utility.subtractDaysFromCurrentDate(3)
        idTokenInfo.notValidBeforeInternal = utility.subtractDaysFromCurrentDate(5)
        return idTokenInfo
    }

    private fun createValidIdTokenInfo(): IdTokenInfo {
        var idTokenInfo = IdTokenInfo()
        idTokenInfo.audienceInternal = UUID.randomUUID().toString()
        idTokenInfo.nonceInternal = TEST_NONCE
        idTokenInfo.expirationInternal = utility.addDaysToCurrentDate(3)
        idTokenInfo.notValidBeforeInternal = utility.getCurrentTimeInUTC()

        return idTokenInfo
    }
}

class TestLoginCallback : LoginCallback, ExchangeTokenCallback {
    private val mockLoginCallback: LoginCallback = Mockito.mock(LoginCallback::class.java)
    private var error: GrabIdPartnerError? = null

    override fun onSuccess() {
        mockLoginCallback.onSuccess()
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
        Mockito.verify(mockLoginCallback, Times(i)).onSuccess()
    }

    fun reset() {
        Mockito.reset(mockLoginCallback)
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

    fun verifyAllRequiredParametersExists(){
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
                this.grabIdTokenInfo?.nonce == idTokenInfo.nonce &&
                this.grabIdTokenInfo?.expiration == idTokenInfo.expiration &&
                this.grabIdTokenInfo?.notValidBefore == idTokenInfo.notValidBefore)
    }
}