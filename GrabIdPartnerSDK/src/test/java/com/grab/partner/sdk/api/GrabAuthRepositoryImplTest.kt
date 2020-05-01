/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk.api

import com.grab.partner.sdk.models.*
import io.reactivex.observers.TestObserver
import org.junit.Test
import java.util.Date

class GrabAuthRepositoryImplTest {
    private val stubGrabSdkApi = StubGrabSdkApi()
    private val grabAuthRepositoryImpl = GrabAuthRepositoryImpl(stubGrabSdkApi)
    private val testObserverDiscoveryResponse = TestObserver<DiscoveryResponse>()
    private val testObserverTokenAPIResponse = TestObserver<TokenAPIResponse>()
    private val testObserverIdTokenInfo = TestObserver<IdTokenInfo>()
    private val testObserverClientPublicInfo = TestObserver<ClientPublicInfo>()

    @Test
    fun callDiscovery() {
        var discoveryResponse = DiscoveryResponse(authorization_endpoint = FAKE_AUTH_ENDPOINT)
        // set the stub return for GrabSdkApi api
        stubGrabSdkApi.setFetchDiscoveryAPIResponse(discoveryResponse)
        // call the repository callDiscovery api
        grabAuthRepositoryImpl.callDiscovery(FAKE_DISCOVERY_ENDPOINT).subscribe(testObserverDiscoveryResponse)

        // verify callDiscovery return using TestObserver
        testObserverDiscoveryResponse.assertComplete()
        testObserverDiscoveryResponse.assertValue(discoveryResponse)
        testObserverDiscoveryResponse.dispose()
    }

    @Test
    fun getToken() {
        var tokenAPIResponse = TokenAPIResponse(access_token = FAKE_ACCESS_TOKEN, token_type = FAKE_TOKEN_TYPE, expires_in = FAKE_EXPIRES_IN, id_token = FAKE_ID_TOKEN, refresh_token = FAKE_REFRESH_TOKEN)
        var tokenRequest = TokenRequest(code = FAKE_CODE, client_id = FAKE_CLIENT_ID, grant_type = FAKE_GRANT_TYPE, redirect_uri = FAKE_REDIRECT_URI, code_verifier = FAKE_CODE_VERIFIER)

        // set the stub return for GrabSdkApi api
        stubGrabSdkApi.setGetTokenAPIResponse(tokenAPIResponse)
        // call the repository getToken api
        grabAuthRepositoryImpl.getToken(FAKE_TOKEN_ENDPOINT, tokenRequest).subscribe(testObserverTokenAPIResponse)

        // verify getToken return using TestObserver
        testObserverTokenAPIResponse.assertComplete()
        testObserverTokenAPIResponse.assertValue(tokenAPIResponse)
        testObserverTokenAPIResponse.dispose()
    }

    @Test
    fun getIdTokenInfo() {
        var idTokenInfo = IdTokenInfo()
        idTokenInfo.audienceInternal = FAKE_AUDIENCE
        idTokenInfo.serviceInternal = FAKE_SERVICE
        idTokenInfo.notValidBeforeInternal = FAKE_NOT_VALID_BEFORE
        idTokenInfo.expirationInternal = FAKE_EXPIRATION
        idTokenInfo.issueDateInternal = FAKE_ISSUE_DATE
        idTokenInfo.issuerInternal = FAKE_ISSUER
        idTokenInfo.tokenIdInternal = FAKE_TOKEN_ID_INTERNAL
        idTokenInfo.partnerIdInternal = FAKE_PARTNER_ID
        idTokenInfo.partnerUserIdInternal = FAKE_PARTNER_USER_ID
        idTokenInfo.nonceInternal = FAKE_NONCE

        // set the stub return for GrabSdkApi api
        stubGrabSdkApi.setGetIdTokenInfoAPIResponse(idTokenInfo)
        // call the repository getIdTokenInfo api
        grabAuthRepositoryImpl.getIdTokenInfo(FAKE_ID_TOKEN_ENDPOINT, FAKE_CLIENT_ID, FAKE_ID_TOKEN, FAKE_NONCE).subscribe(testObserverIdTokenInfo)

        // verify getIdTokenInfo return using TestObserver
        testObserverIdTokenInfo.assertComplete()
        testObserverIdTokenInfo.assertValue(idTokenInfo)
        testObserverIdTokenInfo.dispose()
    }

    @Test
    fun fetchClientPublicInfo() {
        var clientPublicInfo = ClientPublicInfo(listOf(), FAKE_HOME_PAGE_URL, FAKE_LOGO_URL, FAKE_PRIVACY_POLICY_URL, FAKE_PRODUCT_NAME, FAKE_TERMS_OF_SERVICE_URL)
        stubGrabSdkApi.setFetchClientInfo(clientPublicInfo)
        grabAuthRepositoryImpl.fetchClientPublicInfo(FAKE_CLIENT_PUBLIC_ENDPOINT).subscribe(testObserverClientPublicInfo)
        // verify clientPublicInfo return using TestObserver
        testObserverClientPublicInfo.assertComplete()
        testObserverClientPublicInfo.assertValue(clientPublicInfo)
        testObserverClientPublicInfo.dispose()
    }

    companion object {
        private const val FAKE_DISCOVERY_ENDPOINT = "fake_discovery_endpoint"
        private const val FAKE_TOKEN_ENDPOINT = "fake_token_endpoint"
        private const val FAKE_AUTH_ENDPOINT = "fake_auth_endpoint"
        private const val FAKE_ID_TOKEN_ENDPOINT = "fake_id_token_endpoint"
        private const val FAKE_CLIENT_PUBLIC_ENDPOINT = "fake_client_public_endpoint"
        private const val FAKE_ACCESS_TOKEN = "fake_access_token"
        private const val FAKE_TOKEN_TYPE = "fake_token_type"
        private const val FAKE_EXPIRES_IN = "fake_expires_in"
        private const val FAKE_ID_TOKEN = "fake_id_token"
        private const val FAKE_REFRESH_TOKEN = "fake_refresh_token"
        private const val FAKE_CODE = "fake_code"
        private const val FAKE_CLIENT_ID = "fake_client_id"
        private const val FAKE_GRANT_TYPE = "fake_grant_type"
        private const val FAKE_REDIRECT_URI = "fake_redirect_uri"
        private const val FAKE_CODE_VERIFIER = "fake_code_verifier"
        private const val FAKE_AUDIENCE = "fake_audience_internal"
        private const val FAKE_SERVICE = "fake_service"
        private const val FAKE_NONCE = "fake_nonce_internal"
        private val FAKE_NOT_VALID_BEFORE = Date()
        private val FAKE_EXPIRATION = Date()
        private val FAKE_ISSUE_DATE = Date()
        private const val FAKE_PARTNER_ID = "fake_partner_id"
        private const val FAKE_ISSUER = "fake_issuer"
        private const val FAKE_TOKEN_ID_INTERNAL = "fake_token_id_internal"
        private const val FAKE_PARTNER_USER_ID = "fake_partner_user_id"
        private const val FAKE_HOME_PAGE_URL = "home_page_url"
        private const val FAKE_LOGO_URL = "logo_url"
        private const val FAKE_PRIVACY_POLICY_URL = "privacy_policy_url"
        private const val FAKE_PRODUCT_NAME = "product_name"
        private const val FAKE_TERMS_OF_SERVICE_URL = "terms_of_service_url"
        private const val FAKE_ADR_PLAYSTORE_URL = "terms_of_service_url"
    }
}