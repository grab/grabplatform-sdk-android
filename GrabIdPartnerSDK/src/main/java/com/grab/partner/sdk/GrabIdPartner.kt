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
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.grab.partner.sdk.R.string.ERROR_FETCHING_CLIENT_PUBLIC_INFO_URL
import com.grab.partner.sdk.R.string.ERROR_MISSING_ACCESS_TOKEN
import com.grab.partner.sdk.R.string.ERROR_MISSING_ACCESS_TOKEN_EXPIRY
import com.grab.partner.sdk.R.string.ERROR_MISSING_AUTHORIZE_ENDPOINT_IN_DISCOVERY_ENDPOINT
import com.grab.partner.sdk.R.string.ERROR_MISSING_CLIENT_ID
import com.grab.partner.sdk.R.string.ERROR_MISSING_CODE
import com.grab.partner.sdk.R.string.ERROR_MISSING_DISCOVERY_ENDPOINT
import com.grab.partner.sdk.R.string.ERROR_MISSING_ID_TOKEN_VERIFICATION_ENDPOINT_IN_LOGINSESSION
import com.grab.partner.sdk.R.string.ERROR_MISSING_PARTNER_SCOPE
import com.grab.partner.sdk.R.string.ERROR_MISSING_REDIRECT_URI
import com.grab.partner.sdk.R.string.ERROR_MISSING_TOKEN_ENDPOINT_IN_DISCOVERY_ENDPOINT
import com.grab.partner.sdk.R.string.ERROR_MISSING_TOKEN_ENDPOINT_IN_LOGINSESSION
import com.grab.partner.sdk.R.string.ERROR_MISSING_VERIFY_TOKEN_ENDPOINT_IN_DISCOVERY_ENDPOINT
import com.grab.partner.sdk.R.string.ERROR_NONCE_MISMATCH_GET_ID_TOKEN_INFO
import com.grab.partner.sdk.R.string.ERROR_NULL_DISCOVERY_ENDPOINT_RESPONSE
import com.grab.partner.sdk.R.string.ERROR_SDK_IS_NOT_INITIALIZED
import com.grab.partner.sdk.R.string.ERROR_STATE_MISMATCH
import com.grab.partner.sdk.R.string.URL_INVOKED
import com.grab.partner.sdk.api.GrabAuthRepository
import com.grab.partner.sdk.di.components.DaggerMainComponent
import com.grab.partner.sdk.di.modules.AppModule
import com.grab.partner.sdk.keystore.AndroidKeyStoreWrapper
import com.grab.partner.sdk.keystore.CipherWrapper
import com.grab.partner.sdk.keystore.IAndroidKeyStoreWrapper
import com.grab.partner.sdk.keystore.ICipher
import com.grab.partner.sdk.models.*
import com.grab.partner.sdk.scheduleprovider.SchedulerProvider
import com.grab.partner.sdk.utils.LaunchAppForAuthorization
import com.grab.partner.sdk.utils.IUtility
import com.grab.partner.sdk.utils.LogUtils
import com.grab.partner.sdk.utils.ObjectType
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import retrofit2.HttpException
import java.io.PrintWriter
import java.io.StringWriter
import javax.inject.Inject

internal const val PARTNER_CLIENT_ID_ATTRIBUTE = "com.grab.partner.sdk.ClientId"
internal const val PARTNER_REDIRECT_URI_ATTRIBUTE = "com.grab.partner.sdk.RedirectURI"
internal const val PARTNER_ACR_VALUES_ATTRIBUTE = "com.grab.partner.sdk.AcrValues"
internal const val PARTNER_REQUEST_VALUES_ATTRIBUTE = "com.grab.partner.sdk.Request"
internal const val PARTNER_LOGINHINT_VALUES_ATTRIBUTE = "com.grab.partner.sdk.LoginHint"
internal const val PARTNER_PROMPT_VALUE_ATTRIBUTE = "com.grab.partner.sdk.Prompt"
internal const val PARTNER_IDTOKENHINT_VALUES_ATTRIBUTE = "com.grab.partner.sdk.IDTokenHint"
internal const val PARTNER_SCOPE_ATTRIBUTE = "com.grab.partner.sdk.Scope"
internal const val PARTNER_SERVICE_DISCOVERY_URL = "com.grab.partner.sdk.ServiceDiscoveryUrl"
internal const val GRANT_TYPE = "authorization_code"
internal const val UNKNOWN_HTTP_EXCEPTION = "error: unknown http exception"
internal const val CLIENT_ID = "{client_id}"

class GrabIdPartner private constructor() : GrabIdPartnerProtocol {
    @Inject
    internal lateinit var launchAppForAuthorization: LaunchAppForAuthorization
    @Inject
    internal lateinit var grabAuthRepository: GrabAuthRepository
    @Inject
    internal lateinit var schedulerProvider: SchedulerProvider
    @Inject
    internal lateinit var sharedPreferences: SharedPreferences
    @Inject
    internal lateinit var utility: IUtility
    private var applicationContext: Context? = null
    @set:Inject
    internal var androidKeyStoreWrapper: IAndroidKeyStoreWrapper? = null
    @set:Inject
    internal var cipherWrapper: ICipher? = null
    internal lateinit var compositeDisposable: CompositeDisposable

    private object Holder {
        val INSTANCE = GrabIdPartner()
    }

    companion object {
        val instance: GrabIdPartnerProtocol by lazy { Holder.INSTANCE }
        internal var isSdkInitialized: Boolean = false
        internal const val CODE_CHALLENGE_METHOD: String = "S256"
        internal const val RESPONSE_TYPE: String = "code"
        internal const val RESPONSE_STATE: String = "state"
        internal const val HASH_ALGORITHM = "SHA-256"
        internal const val ENCODING_SETTING = Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
    }

    override fun initialize(context: Context) {
        if (isSdkInitialized) {
            return
        }
        // Get the global application context of the current app.
        applicationContext = context.applicationContext
        // create Android keystore and Cipher classes
        applicationContext?.let {
            androidKeyStoreWrapper = AndroidKeyStoreWrapper(it)
        } ?: return

        cipherWrapper = CipherWrapper()
        // Dependency injection
        val component = DaggerMainComponent
                .builder()
                .appModule(AppModule(context))
                .build()

        component.inject(this)
        compositeDisposable = CompositeDisposable()
        launchAppForAuthorization.speedUpChromeTabs()
        isSdkInitialized = true
    }

    /**
     * generate loginSession object
     */
    override fun loadLoginSession(callback: LoginSessionCallback) {
        // verify SDK has been initialized
        if (!isSdkInitialized) {
            callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.LOADLOGINSESSION, GrabIdPartnerErrorCode.sdkNotInitialized, utility.readResourceString(applicationContext, ERROR_SDK_IS_NOT_INITIALIZED), null))
            return
        }
        val loginSession = LoginSession()
        loginSession.codeVerifierInternal = utility.generateCodeVerifier()
        loginSession.stateInternal = utility.getRandomString()
        loginSession.nonceInternal = utility.getRandomString()
        loginSession.codeChallenge = utility.generateCodeChallenge(loginSession.codeVerifier)

        // retrieve all the partner info from Android manifest and validate we have value for clientId, redirectUri and scope
        val clientId = utility.getPartnerInfo(applicationContext, PARTNER_CLIENT_ID_ATTRIBUTE)
        if (clientId == null || clientId.isEmpty()) {
            callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.LOADLOGINSESSION, GrabIdPartnerErrorCode.invalidClientId, utility.readResourceString(applicationContext, ERROR_MISSING_CLIENT_ID), null))
            return
        } else {
            loginSession.clientId = clientId
        }

        val redirectUri = utility.getPartnerInfo(applicationContext, PARTNER_REDIRECT_URI_ATTRIBUTE)
        if (redirectUri == null || redirectUri.isEmpty()) {
            callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.LOADLOGINSESSION, GrabIdPartnerErrorCode.invalidRedirectURI, utility.readResourceString(applicationContext, ERROR_MISSING_REDIRECT_URI), null))
            return
        } else {
            loginSession.redirectUri = redirectUri
        }

        val serviceDiscoveryUrl = utility.getPartnerInfo(applicationContext, PARTNER_SERVICE_DISCOVERY_URL)
        if (serviceDiscoveryUrl == null || serviceDiscoveryUrl.isEmpty()) {
            callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.LOADLOGINSESSION, GrabIdPartnerErrorCode.invalidDiscoveryEndpoint, utility.readResourceString(applicationContext, ERROR_MISSING_DISCOVERY_ENDPOINT), null))
            return
        } else {
            loginSession.serviceDiscoveryUrl = serviceDiscoveryUrl
        }

        val scope = utility.getPartnerInfo(applicationContext, PARTNER_SCOPE_ATTRIBUTE)
        if (scope == null || scope.isEmpty()) {
            callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.LOADLOGINSESSION, GrabIdPartnerErrorCode.invalidPartnerScope, utility.readResourceString(applicationContext, ERROR_MISSING_PARTNER_SCOPE), null))
            return
        } else {
            loginSession.scope = scope
        }

        loginSession.acrValues = utility.getPartnerInfo(applicationContext, PARTNER_ACR_VALUES_ATTRIBUTE) ?: ""
        loginSession.request = utility.getPartnerInfo(applicationContext, PARTNER_REQUEST_VALUES_ATTRIBUTE) ?: ""
        loginSession.loginHint = utility.getPartnerInfo(applicationContext, PARTNER_LOGINHINT_VALUES_ATTRIBUTE) ?: ""
        loginSession.idTokenHint = utility.getPartnerInfo(applicationContext, PARTNER_IDTOKENHINT_VALUES_ATTRIBUTE)
                ?: ""
        loginSession.prompt = utility.getPartnerInfo(applicationContext, PARTNER_PROMPT_VALUE_ATTRIBUTE) ?: ""

        callback.onSuccess(loginSession)
    }

    override fun loadLoginSession(state: String, clientId: String, redirectUri: String, serviceDiscoveryUrl: String,
                                  scope: String, acrValues: String?, request: String?, loginHint: String?, idTokenHint: String?, callback:
                                  LoginSessionCallback, prompt: String?) {
        // verify SDK has been initialized
        if (!isSdkInitialized) {
            callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.LOADLOGINSESSION, GrabIdPartnerErrorCode.sdkNotInitialized, utility.readResourceString(applicationContext, ERROR_SDK_IS_NOT_INITIALIZED), null))
            return
        }
        val loginSession = LoginSession()
        loginSession.codeVerifierInternal = utility.generateCodeVerifier()
        loginSession.stateInternal = state
        loginSession.nonceInternal = utility.getRandomString()
        loginSession.codeChallenge = utility.generateCodeChallenge(loginSession.codeVerifier)

        if (clientId.isEmpty()) {
            callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.LOADLOGINSESSION, GrabIdPartnerErrorCode.invalidClientId, utility.readResourceString(applicationContext, ERROR_MISSING_CLIENT_ID), null))
            return
        } else {
            loginSession.clientId = clientId
        }

        if (redirectUri.isEmpty()) {
            callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.LOADLOGINSESSION, GrabIdPartnerErrorCode.invalidRedirectURI, utility.readResourceString(applicationContext, ERROR_MISSING_REDIRECT_URI), null))
            return
        } else {
            loginSession.redirectUri = redirectUri
        }

        if (serviceDiscoveryUrl.isEmpty()) {
            callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.LOADLOGINSESSION, GrabIdPartnerErrorCode.invalidDiscoveryEndpoint, utility.readResourceString(applicationContext, ERROR_MISSING_DISCOVERY_ENDPOINT), null))
            return
        } else {
            loginSession.serviceDiscoveryUrl = serviceDiscoveryUrl
        }

        if (scope.isEmpty()) {
            callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.LOADLOGINSESSION, GrabIdPartnerErrorCode.invalidPartnerScope, utility.readResourceString(applicationContext, ERROR_MISSING_PARTNER_SCOPE), null))
            return
        } else {
            loginSession.scope = scope
        }

        loginSession.acrValues = acrValues ?: ""
        loginSession.request = request ?: ""
        loginSession.loginHint = loginHint ?: ""
        loginSession.idTokenHint = idTokenHint ?: ""
        loginSession.prompt = prompt ?: ""

        callback.onSuccess(loginSession)
    }

    /**
     * Login user using Grab ID Partner SDK
     */
    override fun login(loginSession: LoginSession, context: Context, callback: LoginCallback) {
        // validate if loginSession has all the required parameters
        if (loginSession.clientId.isBlank()) {
            callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.LOGIN, GrabIdPartnerErrorCode.invalidClientId, utility.readResourceString(applicationContext, ERROR_MISSING_CLIENT_ID), null))
            return
        }
        if (loginSession.redirectUri.isBlank()) {
            callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.LOGIN, GrabIdPartnerErrorCode.invalidRedirectURI, utility.readResourceString(applicationContext, ERROR_MISSING_REDIRECT_URI), null))
            return
        }
        if (loginSession.scope.isBlank()) {
            callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.LOGIN, GrabIdPartnerErrorCode.invalidPartnerScope, utility.readResourceString(applicationContext, ERROR_MISSING_PARTNER_SCOPE), null))
            return
        }
        if (loginSession.serviceDiscoveryUrl.isBlank()) {
            callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.LOGIN, GrabIdPartnerErrorCode.invalidDiscoveryEndpoint, utility.readResourceString(applicationContext, ERROR_MISSING_DISCOVERY_ENDPOINT), null))
            return
        }

        // check if user has previous valid access token in shared preference
        compositeDisposable.add(
                retrieveLoginSessionFromCache(loginSession)
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe({ loginSessionFromCache ->
                            if (loginSessionFromCache != null) {
                                utility.cloneLoginSession(loginSessionFromCache, loginSession)
                                callback.onSuccess()
                                return@subscribe
                            }
                        }, {
                            // log the exception and let user continue the login flow
                            val sw = StringWriter()
                            it.printStackTrace(PrintWriter(sw))
                            LogUtils.debug("login", sw.toString())

                            // call the discovery endpoint to fetch all the endpoints even in error case as we don't want to block the user
                            callDiscovery(context, loginSession, callback)
                        }, {
                            // no cache data is available
                            // call the discovery endpoint to fetch all the endpoints
                            callDiscovery(context, loginSession, callback)
                        })
        )
    }

    /**
     * Login user using Grab ID Partner SDK with LoginCallbackV2 callback
     * This API will invoke callback.onSuccessCache() when loginSession will be retrieved from cache
     */
    override fun login(loginSession: LoginSession, context: Context, callback: LoginCallbackV2) {
        // validate if loginSession has all the required parameters
        if (loginSession.clientId.isBlank()) {
            callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.LOGIN, GrabIdPartnerErrorCode.invalidClientId, utility.readResourceString(applicationContext, ERROR_MISSING_CLIENT_ID), null))
            return
        }
        if (loginSession.redirectUri.isBlank()) {
            callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.LOGIN, GrabIdPartnerErrorCode.invalidRedirectURI, utility.readResourceString(applicationContext, ERROR_MISSING_REDIRECT_URI), null))
            return
        }
        if (loginSession.scope.isBlank()) {
            callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.LOGIN, GrabIdPartnerErrorCode.invalidPartnerScope, utility.readResourceString(applicationContext, ERROR_MISSING_PARTNER_SCOPE), null))
            return
        }
        if (loginSession.serviceDiscoveryUrl.isBlank()) {
            callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.LOGIN, GrabIdPartnerErrorCode.invalidDiscoveryEndpoint, utility.readResourceString(applicationContext, ERROR_MISSING_DISCOVERY_ENDPOINT), null))
            return
        }

        // check if user has previous valid access token in shared preference
        compositeDisposable.add(
                retrieveLoginSessionFromCache(loginSession)
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe({ loginSessionFromCache ->
                            if (loginSessionFromCache != null) {
                                utility.cloneLoginSession(loginSessionFromCache, loginSession)
                                callback.onSuccessCache()
                                return@subscribe
                            }
                        }, {
                            // log the exception and let user continue the login flow
                            val sw = StringWriter()
                            it.printStackTrace(PrintWriter(sw))
                            LogUtils.debug("login", sw.toString())

                            // call the discovery endpoint to fetch all the endpoints even in error case as we don't want to block the user
                            callDiscovery(context, loginSession, callback)
                        }, {
                            // no cache data is available
                            // call the discovery endpoint to fetch all the endpoints
                            callDiscovery(context, loginSession, callback)
                        })
        )
    }

    /**
    Get the access token from token exchange endpoint
     */
    override fun exchangeToken(loginSession: LoginSession, redirectUrl: String, callback: ExchangeTokenCallback) {
        // validate redirectUrl is not empty or null
        if (redirectUrl.isNullOrBlank()) {
            callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.EXCHANGETOKEN, GrabIdPartnerErrorCode.invalidRedirectURI, utility.readResourceString(applicationContext, ERROR_MISSING_REDIRECT_URI), null))
            return
        }

        // obtain the code and state from the redirect uri
        val code = utility.getURLParam(RESPONSE_TYPE, redirectUrl)
        val state = utility.getURLParam(RESPONSE_STATE, redirectUrl)

        // validate code is not null or empty string
        if (code == null || code.isEmpty()) {
            callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.EXCHANGETOKEN, GrabIdPartnerErrorCode.invalidCode, utility.readResourceString(applicationContext, ERROR_MISSING_CODE), null))
            return
        }

        // check state match with loginSession state
        if (state != loginSession.state) {
            callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.EXCHANGETOKEN, GrabIdPartnerErrorCode.stateMismatch, utility.readResourceString(applicationContext, ERROR_STATE_MISMATCH), null))
            return
        }

        // check if we have valid token endpoint inside loginSession otherwise throw error
        val tokenEndPoint = loginSession.tokenEndpoint
        if (tokenEndPoint == null || tokenEndPoint.isEmpty()) {
            callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.EXCHANGETOKEN, GrabIdPartnerErrorCode.errorInLoginSessionObject, utility.readResourceString(applicationContext, ERROR_MISSING_TOKEN_ENDPOINT_IN_LOGINSESSION), null))
            return
        }

        // save the code and state in the loginSession object
        loginSession.codeInternal = code
        loginSession.stateInternal = state

        compositeDisposable.add(grabAuthRepository.getToken(tokenEndPoint, TokenRequest(code, loginSession.clientId, GRANT_TYPE, loginSession.redirectUri, loginSession.codeVerifier))
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .flatMapCompletable {
                    processLoginSessionResponse(loginSession, it, callback)
                }
                .subscribe({
                }, { error ->
                    var errorJsonString = ""
                    if (error is HttpException) {
                        errorJsonString = error.response().errorBody()?.string()
                                ?: UNKNOWN_HTTP_EXCEPTION
                    }
                    callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.EXCHANGETOKEN, GrabIdPartnerErrorCode.network, errorJsonString, error))
                    return@subscribe
                }))
    }

    private fun processLoginSessionResponse(loginSession: LoginSession, result: TokenAPIResponse, callback: ExchangeTokenCallback): Completable {
        // validate that response from the exchange token endpoint has required parameters
        val accessToken = result.access_token
        if (!accessToken.isNullOrBlank()) {
            // update the loginSession object
            loginSession.accessTokenInternal = accessToken
        } else {
            callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.EXCHANGETOKEN, GrabIdPartnerErrorCode.missingAccessToken, utility.readResourceString(applicationContext, ERROR_MISSING_ACCESS_TOKEN), null))
            return Completable.complete()
        }

        val expiresIn = result.expires_in
        if (!expiresIn.isNullOrBlank()) {
            loginSession.accessTokenExpiresAtInternal = utility.addSecondsToCurrentDate(expiresIn)
        } else {
            callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.EXCHANGETOKEN, GrabIdPartnerErrorCode.missingAccessTokenExpiry, utility.readResourceString(applicationContext, ERROR_MISSING_ACCESS_TOKEN_EXPIRY), null))
            return Completable.complete()
        }

        // no error for missing id_token, id_token will only be part of the /token API response if 'openid' is part of the scope
        if (result.id_token != null) {
            // update the loginSession object
            loginSession.idTokenInternal = result.id_token
        }

        // no error for missing refresh_token as this is currently optional
        if (result.refresh_token != null) {
            // update the loginSession object
            loginSession.refreshTokenInternal = result.refresh_token
        }

        if (result.token_type != null) {
            loginSession.tokenTypeInternal = result.token_type
        }

        // serialize loginSession object
        val gson = Gson()
        val serializedLoginSession = gson.toJson(loginSession)

        // encrypt entire loginSession object
        var encryptedString: String? = null
        try {
            val keyPair = androidKeyStoreWrapper?.createAndroidKeyStoreAsymmetricKey(utility.generateKeystoreAlias(loginSession, ObjectType.LOGIN_SESSION))
            encryptedString = cipherWrapper?.encrypt(serializedLoginSession, keyPair?.public)
        } catch (exception: Exception) {
            // log the exception and let user continue the login flow
            val sw = StringWriter()
            exception.printStackTrace(PrintWriter(sw))
            LogUtils.debug("login", sw.toString())
        }

        // save loginSession to shared preference
        if (encryptedString != null) {
            sharedPreferences.let {
                utility.saveObjectsToSharedPref(loginSession, encryptedString, sharedPreferences, ObjectType.LOGIN_SESSION)
            }
        }
        callback.onSuccess()
        return Completable.complete()
    }

    /**
     * Validate received token is valid using token info endpoint
     */
    override fun getIdTokenInfo(loginSession: LoginSession, callback: GetIdTokenInfoCallback) {
        val idTokenVerificationEndpoint = loginSession.idTokenVerificationEndpoint
        if (idTokenVerificationEndpoint.isNullOrBlank()) {
            callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.GETIDTOKENINFO, GrabIdPartnerErrorCode.errorInLoginSessionObject, utility.readResourceString(applicationContext, ERROR_MISSING_ID_TOKEN_VERIFICATION_ENDPOINT_IN_LOGINSESSION), null))
            return
        }

        // check if user has valid idTokenInfo in the shared preference
        compositeDisposable.add(
                retrieveIdTokenInfoFromCache(loginSession)
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe({ idTokenInfoFromSharedPref ->
                            if (idTokenInfoFromSharedPref != null) {
                                callback.onSuccess(idTokenInfoFromSharedPref)
                                return@subscribe
                            }
                        }, {
                            callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.GETIDTOKENINFO, GrabIdPartnerErrorCode.errorRetrievingObjectFromSharedPref, it.localizedMessage, null))
                            return@subscribe
                        }, {
                            // no cache available
                            compositeDisposable.add(grabAuthRepository.getIdTokenInfo(idTokenVerificationEndpoint, loginSession.clientId, loginSession.idToken, loginSession.nonce)
                                    .subscribeOn(schedulerProvider.io())
                                    .observeOn(schedulerProvider.ui())
                                    .flatMapCompletable { processIdTokenInfoApiResponse(idTokenInfo = it, loginSession = loginSession, callback = callback) }
                                    .subscribe({}, { error ->
                                        var errorJsonString = ""
                                        if (error is HttpException) {
                                            errorJsonString = error.response().errorBody()?.string()
                                                    ?: UNKNOWN_HTTP_EXCEPTION
                                        }
                                        callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.GETIDTOKENINFO, GrabIdPartnerErrorCode.errorInGetIdTokenInfo, errorJsonString, error))
                                        return@subscribe
                                    }))
                        })
        )
    }

    private fun processIdTokenInfoApiResponse(idTokenInfo: IdTokenInfo, loginSession: LoginSession, callback: GetIdTokenInfoCallback): Completable {
        // validate nonce is matching with the one received from server
        if (loginSession.nonce != idTokenInfo.nonce) {
            callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.GETIDTOKENINFO, GrabIdPartnerErrorCode.errorInGetIdTokenInfo, utility.readResourceString(applicationContext, ERROR_NONCE_MISMATCH_GET_ID_TOKEN_INFO), null))
            return Completable.complete()
        }

        // serialize idTokenInfo object
        val gson = Gson()
        val serializedIdTokenInfo = gson.toJson(idTokenInfo)

        // encrypt entire idTokenInfo object, Here we intentionally skipping the error callback if any exception occur. For any exception we still wants to send the token info to user and will skip the caching part.
        var encryptedString: String? = null
        try {
            val keyPair = androidKeyStoreWrapper?.createAndroidKeyStoreAsymmetricKey(utility.generateKeystoreAlias(loginSession, ObjectType.ID_TOKEN_INFO))
            encryptedString = cipherWrapper?.encrypt(serializedIdTokenInfo, keyPair?.public)
        } catch (exception: Exception) {
            // log the exception and let user continue the login flow
            val sw = StringWriter()
            exception.printStackTrace(PrintWriter(sw))
            LogUtils.debug("login", sw.toString())
        }

        // save idTokenInfoToSaveInSharedPreference to shared preference
        if (encryptedString != null) {
            utility.saveObjectsToSharedPref(loginSession, encryptedString, sharedPreferences, ObjectType.ID_TOKEN_INFO)
        }
        callback.onSuccess(idTokenInfo)
        return Completable.complete()
    }

    /**
     * Clear all login information from shared preference and Android keystore
     */
    override fun logout(loginSession: LoginSession, callback: LogoutCallback) {
        try {
            // delete the entry from shared pref and key from Android KeyStore
            deleteEntriesFromSharedPreferenceAndKeystore(loginSession, sharedPreferences)
            utility.clearLoginSession(loginSession)
            callback.onSuccess()
        } catch (exception: Exception) {
            callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.LOGOUT, GrabIdPartnerErrorCode.errorInLogout, exception.localizedMessage, null))
        }
    }

    /**
     * Unsubscribe all observable subscription and clearing other objects
     */
    override fun teardown() {
        try {
            applicationContext = null
            androidKeyStoreWrapper = null
            cipherWrapper = null
            isSdkInitialized = false
            // dispose all observable subscription
            compositeDisposable.clear()
        } catch (exception: Exception) {
            if (!compositeDisposable.isDisposed) {
                compositeDisposable.clear()
            }
            LogUtils.debug("teardown", exception.toString())
        }
    }

    /**
     * Validate if access token is still valid
     */
    override fun isValidAccessToken(loginSession: LoginSession): Boolean {
        val accessTokenExpireAt = loginSession.accessTokenExpiresAt
        if (accessTokenExpireAt == null || accessTokenExpireAt < utility.getCurrentTimeInUTC()) {
            return false
        }
        return true
    }

    /**
     * Validate whether id token is still valid
     */
    override fun isValidIdToken(idTokenInfo: IdTokenInfo): Boolean {
        if (idTokenInfo.nonce.isNullOrBlank())
            return false

        val idTokenExpireAt = idTokenInfo.expiration
        val idTokenNotValidBefore = idTokenInfo.notValidBefore
        val currentTimeInUTC = utility.getCurrentTimeInUTC()

        if (idTokenExpireAt == null || idTokenNotValidBefore == null || idTokenExpireAt < currentTimeInUTC || idTokenNotValidBefore > currentTimeInUTC) {
            return false
        }

        return true
    }

    /**
     * Call the discovery endpoint to get the URLs
     */
    private fun callDiscovery(context: Context, loginSession: LoginSession, callback: LoginCallback) {
        compositeDisposable.add(grabAuthRepository.callDiscovery(loginSession.serviceDiscoveryUrl)
                .flatMap { discoveryResponse ->
                    // replace "{client_id}" string to get the final client_public_info_endpoint url
                    discoveryResponse.client_public_info_endpoint = discoveryResponse.client_public_info_endpoint.replace(CLIENT_ID, loginSession.clientId)
                    grabAuthRepository.fetchClientPublicInfo(discoveryResponse.client_public_info_endpoint)
                            .map { discoveryResponse to it }
                            .onErrorResumeNext { it: Throwable ->
                                Observable.error<Pair<DiscoveryResponse, ClientPublicInfo>>(CustomInternalError(domain = GrabIdPartnerErrorDomain.CLIENTPUBLICINFO,
                                        code = GrabIdPartnerErrorCode.errorInClientPublicInfoEndpoint,
                                        extraMessage = "${utility.readResourceString(applicationContext, ERROR_FETCHING_CLIENT_PUBLIC_INFO_URL)} ${utility.readResourceString(applicationContext, URL_INVOKED)} ${discoveryResponse.client_public_info_endpoint}", cause = it))
                            }
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe({ result ->
                    // validate that response from the discovery endpoint has required parameters
                    if (result == null) {
                        callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.SERVICEDISCOVERY, GrabIdPartnerErrorCode.errorInDiscoveryEndpoint, utility.readResourceString(applicationContext, ERROR_NULL_DISCOVERY_ENDPOINT_RESPONSE), null))
                        return@subscribe
                    }
                    val discoveryResponse = result.first
                    val clientPublicInfo = result.second

                    if (!discoveryResponse.authorization_endpoint.isNullOrBlank()) {
                        loginSession.authorizationEndpoint = discoveryResponse.authorization_endpoint
                    } else {
                        callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.SERVICEDISCOVERY, GrabIdPartnerErrorCode.errorInDiscoveryEndpoint, utility.readResourceString(applicationContext, ERROR_MISSING_AUTHORIZE_ENDPOINT_IN_DISCOVERY_ENDPOINT), null))
                        return@subscribe
                    }

                    if (!discoveryResponse.token_endpoint.isNullOrBlank()) {
                        loginSession.tokenEndpoint = discoveryResponse.token_endpoint
                    } else {
                        callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.SERVICEDISCOVERY, GrabIdPartnerErrorCode.errorInDiscoveryEndpoint, utility.readResourceString(applicationContext, ERROR_MISSING_TOKEN_ENDPOINT_IN_DISCOVERY_ENDPOINT), null))
                        return@subscribe
                    }

                    if (!discoveryResponse.id_token_verification_endpoint.isNullOrBlank()) {
                        loginSession.idTokenVerificationEndpoint = discoveryResponse.id_token_verification_endpoint
                    } else {
                        callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.SERVICEDISCOVERY, GrabIdPartnerErrorCode.errorInDiscoveryEndpoint, utility.readResourceString(applicationContext, ERROR_MISSING_VERIFY_TOKEN_ENDPOINT_IN_DISCOVERY_ENDPOINT), null))
                        return@subscribe
                    }
                    // if loginSession object contains login_hint or id_token_hint we want to open the Chrome Custom Tab to complete the OAuth flow
                    if (loginSession.loginHint?.isNotBlank() == true || loginSession.idTokenHint?.isNotBlank() == true || loginSession.prompt?.isNotBlank() == true) {
                        launchAppForAuthorization.launchOAuthFlow(context, loginSession, callback)
                    } else {
                        // check if we have native app installed otherwise launch the OAuth flow using Chrome Custom Tab
                        utility.isPackageInstalled(clientPublicInfo.custom_protocols, context.packageManager)
                                .subscribeOn(schedulerProvider.io())
                                .observeOn(schedulerProvider.ui())
                                .subscribeBy(onSuccess = { protocolInfo ->
                                    // if we find the package then launch the native app otherwise open the Chrome Custom Tab
                                    if (protocolInfo != null) {
                                        loginSession.deeplinkUriInternal = protocolInfo.protocol_adr
                                        launchAppForAuthorization.launchOAuthFlow(context, loginSession, callback, true)
                                    }
                                }, onError = {
                                    // if we face any error then launch Chrome browser flow
                                    launchAppForAuthorization.launchOAuthFlow(context, loginSession, callback)
                                    LogUtils.debug("callDiscovery", it.localizedMessage)
                                }, onComplete = {
                                    // if we're here means we haven't find the required native app so will launch the Chrome browser flow
                                    launchAppForAuthorization.launchOAuthFlow(context, loginSession, callback)
                                })
                    }

                }, { error ->
                    var errorJsonString = ""
                    if (error is HttpException) {
                        errorJsonString = error.response().errorBody()?.string()
                                ?: UNKNOWN_HTTP_EXCEPTION
                    }
                    if (error is CustomInternalError) {
                        callback.onError(GrabIdPartnerError(error.domain, error.code, errorJsonString + " " + error.extraMessage, error))
                    } else {
                        callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.SERVICEDISCOVERY, GrabIdPartnerErrorCode.errorInDiscoveryEndpoint, errorJsonString, error))
                    }
                    return@subscribe
                }))
    }

    /**
     * Retrieve the loginSession from shared preference if exist, then decrypt the encrypted string using key store keys
     */
    private fun retrieveLoginSessionFromCache(loginSession: LoginSession): Maybe<LoginSession> {
        // check if we have valid token for this clientId and scope
        val encryptionData = utility.retrieveObjectFromSharedPref(loginSession, sharedPreferences, ObjectType.LOGIN_SESSION)
        if (encryptionData != null) {
            // decrypt the loginSessionEncrypted data
            val loginSessionDecryptedString: String?
            try {
                val keyPair = androidKeyStoreWrapper?.getAndroidKeyStoreAsymmetricKeyPair(utility.generateKeystoreAlias(loginSession, ObjectType.LOGIN_SESSION))
                if (keyPair == null) {
                    deleteEntriesFromSharedPreferenceAndKeystore(loginSession, sharedPreferences)
                    return Maybe.empty()
                }
                loginSessionDecryptedString = cipherWrapper?.decrypt(encryptionData, keyPair.private)
            } catch (exception: Exception) {
                // delete this entry from shared preferences and also delete the Android keystore keys, no use of keeping that as
                // we not able to decrypt the encrypted string, this is to avoid any future failure
                deleteEntriesFromSharedPreferenceAndKeystore(loginSession, sharedPreferences)

                // write the error in debug log
                val sw = StringWriter()
                exception.printStackTrace(PrintWriter(sw))
                LogUtils.debug("retrieveLoginSessionFromCache", sw.toString())
                return Maybe.empty()
            }

            // deserialize to LoginSession object
            val loginSessionFromCache = loginSessionDecryptedString.let { utility.deSerializeToLoginSession(it.toString()) }

            if (loginSessionFromCache != null) {
                // check whether the accessToken still valid if not then clear all entries from shared preference and Android keystore
                if (!isValidAccessToken(loginSessionFromCache)) {
                    try {
                        deleteEntriesFromSharedPreferenceAndKeystore(loginSessionFromCache, sharedPreferences)
                    } catch (exception: java.lang.Exception) {
                        // write the error in debug log
                        val sw = StringWriter()
                        exception.printStackTrace(PrintWriter(sw))
                        LogUtils.debug("retrieveLoginSessionFromCache", sw.toString())
                    }
                    return Maybe.empty()

                } else {
                    return Maybe.just(loginSessionFromCache)
                }

            } else
                return Maybe.empty()
        } else
            return Maybe.empty()
    }

    /**
     * Retrieve the idTokenInfo from shared preference if exist, then decrypt the encrypted string using key store keys
     */
    private fun retrieveIdTokenInfoFromCache(loginSession: LoginSession): Maybe<IdTokenInfo> {
        // check if we have valid IdTokenInfo for this clientId and scope
        val encryptionData = utility.retrieveObjectFromSharedPref(loginSession, sharedPreferences, ObjectType.ID_TOKEN_INFO)
        if (encryptionData != null) {
            // decrypt the idTokenInfoEncryptedData data
            val idTokenInfoDecryptedString: String?
            try {
                val keyPair = androidKeyStoreWrapper?.getAndroidKeyStoreAsymmetricKeyPair(utility.generateKeystoreAlias(loginSession, ObjectType.ID_TOKEN_INFO))
                if (keyPair == null) {
                    deleteEntriesFromSharedPreferenceAndKeystore(loginSession, sharedPreferences)
                    return Maybe.empty()
                }

                idTokenInfoDecryptedString = cipherWrapper?.decrypt(encryptionData, keyPair.private)
            } catch (exception: Exception) {
                // delete this entry from shared preferences and also delete the Android keystore keys, no use of keeping that as
                // we not able to decrypt the encrypted string, this is to avoid any future failure
                deleteEntriesFromSharedPreferenceAndKeystore(loginSession, sharedPreferences)

                // write the error in debug log
                val sw = StringWriter()
                exception.printStackTrace(PrintWriter(sw))
                LogUtils.debug("retrieveIdTokenInfoFromCache", sw.toString())
                return Maybe.empty()
            }

            // deserialize to LoginSession object
            val idTokenInfo = idTokenInfoDecryptedString.let { utility.deSerializeToIdTokenInfo(it.toString()) }

            if (idTokenInfo != null) {
                // check whether the idToken still valid if not then clear all entries from shared preference and Android keystore
                if (!isValidIdToken(idTokenInfo)) {
                    try {
                        // idToken expired deleting the entry from shared preferences and also delete the Android keystore keys
                        deleteEntriesFromSharedPreferenceAndKeystore(loginSession, sharedPreferences)
                    } catch (exception: java.lang.Exception) {
                        // write the error in debug log
                        val sw = StringWriter()
                        exception.printStackTrace(PrintWriter(sw))
                        LogUtils.debug("retrieveIdTokenInfoFromCache", sw.toString())
                    }
                    return Maybe.empty()
                } else {
                    return Maybe.just(idTokenInfo)
                }

            } else
                return Maybe.empty()
        } else
            return Maybe.empty()
    }

    /**
     * Method to delete all the entries from shared preference and keystore
     */
    private fun deleteEntriesFromSharedPreferenceAndKeystore(loginSession: LoginSession, sharedPreferences: SharedPreferences) {
        utility.deleteObjectsFromSharedPref(loginSession, sharedPreferences, ObjectType.LOGIN_SESSION)
        utility.deleteObjectsFromSharedPref(loginSession, sharedPreferences, ObjectType.ID_TOKEN_INFO)
        androidKeyStoreWrapper?.deleteKeys()

        // remove the token information from loginSession object
        loginSession.accessTokenInternal = ""
        loginSession.idTokenInternal = ""
        loginSession.refreshTokenInternal = ""
        loginSession.accessTokenExpiresAtInternal = null
    }
}

/**
 * Throwable error that can be used inside sdk, internal to sdk only not to be used as final error type to send to sdk consumer
 */
class CustomInternalError(var domain: GrabIdPartnerErrorDomain, var code: GrabIdPartnerErrorCode, var extraMessage: String = "", cause: Throwable) : Throwable(cause)

interface LoginSessionCallback {
    fun onSuccess(loginSession: LoginSession)
    fun onError(grabIdPartnerError: GrabIdPartnerError)
}

interface LoginCallback {
    fun onSuccess()
    fun onError(grabIdPartnerError: GrabIdPartnerError)
}

interface LoginCallbackV2 : LoginCallback {
    fun onSuccessCache()
}

interface ExchangeTokenCallback {
    fun onSuccess()
    fun onError(grabIdPartnerError: GrabIdPartnerError)
}

interface GetIdTokenInfoCallback {
    fun onSuccess(idTokenInfo: IdTokenInfo)
    fun onError(grabIdPartnerError: GrabIdPartnerError)
}

interface LogoutCallback {
    fun onSuccess()
    fun onError(grabIdPartnerError: GrabIdPartnerError)
}