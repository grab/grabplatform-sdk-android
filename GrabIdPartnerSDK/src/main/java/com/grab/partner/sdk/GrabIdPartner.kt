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
import com.google.gson.Gson
import com.grab.partner.sdk.R.string.ERROR_MISSING_ACCESS_TOKEN
import com.grab.partner.sdk.R.string.ERROR_MISSING_ACCESS_TOKEN_EXPIRY
import com.grab.partner.sdk.R.string.ERROR_MISSING_AUTHORIZE_ENDPOINT_IN_DISCOVERY_ENDPOINT
import com.grab.partner.sdk.R.string.ERROR_MISSING_CLIENT_ID
import com.grab.partner.sdk.R.string.ERROR_MISSING_CODE
import com.grab.partner.sdk.R.string.ERROR_MISSING_DISCOVERY_ENDPOINT
import com.grab.partner.sdk.R.string.ERROR_MISSING_ID_TOKEN
import com.grab.partner.sdk.R.string.ERROR_MISSING_ID_TOKEN_VERIFICATION_ENDPOINT_IN_LOGINSESSION
import com.grab.partner.sdk.R.string.ERROR_MISSING_PARTNER_SCOPE
import com.grab.partner.sdk.R.string.ERROR_MISSING_REDIRECT_URI
import com.grab.partner.sdk.R.string.ERROR_MISSING_TOKEN_ENDPOINT_IN_DISCOVERY_ENDPOINT
import com.grab.partner.sdk.R.string.ERROR_MISSING_TOKEN_ENDPOINT_IN_LOGINSESSION
import com.grab.partner.sdk.R.string.ERROR_MISSING_VERIFY_TOKEN_ENDPOINT_IN_DISCOVERY_ENDPOINT
import com.grab.partner.sdk.R.string.ERROR_NONCE_MISMATCH_GET_ID_TOKEN_INFO
import com.grab.partner.sdk.R.string.ERROR_NULL_DISCOVERY_ENDPOINT_RESPONSE
import com.grab.partner.sdk.R.string.ERROR_NULL_EXCHANGE_TOKEN_RESPONSE
import com.grab.partner.sdk.R.string.ERROR_SDK_IS_NOT_INITIALIZED
import com.grab.partner.sdk.R.string.ERROR_STATE_MISMATCH
import com.grab.partner.sdk.api.GrabAuthRepository
import com.grab.partner.sdk.di.components.DaggerMainComponent
import com.grab.partner.sdk.di.modules.AppModule
import com.grab.partner.sdk.keystore.AndroidKeyStoreWrapper
import com.grab.partner.sdk.keystore.CipherWrapper
import com.grab.partner.sdk.keystore.IAndroidKeyStoreWrapper
import com.grab.partner.sdk.keystore.ICipher
import com.grab.partner.sdk.models.GrabIdPartnerError
import com.grab.partner.sdk.models.GrabIdPartnerErrorCode
import com.grab.partner.sdk.models.GrabIdPartnerErrorDomain
import com.grab.partner.sdk.models.IdTokenInfo
import com.grab.partner.sdk.models.LoginSession
import com.grab.partner.sdk.models.TokenRequest
import com.grab.partner.sdk.scheduleprovider.SchedulerProvider
import com.grab.partner.sdk.utils.IChromeCustomTab
import com.grab.partner.sdk.utils.IUtility
import com.grab.partner.sdk.utils.LogUtils
import com.grab.partner.sdk.utils.ObjectType
import retrofit2.HttpException
import java.io.PrintWriter
import java.io.StringWriter
import javax.inject.Inject

internal const val PARTNER_CLIENT_ID_ATTRIBUTE = "com.grab.partner.sdk.ClientId"
internal const val PARTNER_REDIRECT_URI_ATTRIBUTE = "com.grab.partner.sdk.RedirectURI"
internal const val PARTNER_ACR_VALUES_ATTRIBUTE = "com.grab.partner.sdk.AcrValues"
internal const val PARTNER_REQUEST_VALUES_ATTRIBUTE = "com.grab.partner.sdk.Request"
internal const val PARTNER_LOGINHINT_VALUES_ATTRIBUTE = "com.grab.partner.sdk.LoginHint"
internal const val PARTNER_SCOPE_ATTRIBUTE = "com.grab.partner.sdk.Scope"
internal const val PARTNER_SERVICE_DISCOVERY_URL = "com.grab.partner.sdk.ServiceDiscoveryUrl"
internal const val GRANT_TYPE = "authorization_code"

class GrabIdPartner private constructor() : GrabIdPartnerProtocol {
    @Inject
    internal lateinit var chromeCustomTab: IChromeCustomTab
    @Inject
    internal lateinit var grabAuthRepository: GrabAuthRepository
    @Inject
    internal lateinit var schedulerProvider: SchedulerProvider
    @Inject
    internal lateinit var sharedPreferences: SharedPreferences
    @Inject
    internal lateinit var utility: IUtility
    @Inject
    internal lateinit var androidKeyStoreWrapper: IAndroidKeyStoreWrapper
    @Inject
    internal lateinit var cipherWrapper: ICipher

    private object Holder {
        val INSTANCE = GrabIdPartner()
    }

    companion object {
        val instance: GrabIdPartner by lazy { Holder.INSTANCE }
        internal var isSdkInitialized: Boolean = false
        internal lateinit var applicationContext: Context
        internal const val CODE_CHALLENGE_METHOD: String = "S256"
        internal const val RESPONSE_TYPE: String = "code"
        internal const val RESPONSE_STATE: String = "state"
        internal const val HASH_ALGORITHM = "SHA-256"
        internal const val ENCODING_SETTING = Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
    }

    fun initialize(context: Context) {
        if (isSdkInitialized) {
            return
        }

        // Get the global application context of the current app.
        applicationContext = context.applicationContext
        // create Android keystore and Cipher classes
        androidKeyStoreWrapper = AndroidKeyStoreWrapper(applicationContext)
        cipherWrapper = CipherWrapper()

        // Dependency injection
        var component = DaggerMainComponent
                .builder()
                .appModule(AppModule(context))
                .build()

        component.inject(this)
        chromeCustomTab.speedUpChromeTabs()
        isSdkInitialized = true
    }

    /**
     * generate loginSession object
     */
    override fun loadLoginSession(callback: LoginSessionCallback) {
        // verify SDK has been initialized
        if (!isSdkInitialized) {
            callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.LOADLOGINSESSION, GrabIdPartnerErrorCode.sdkNotInitialized, utility.readResourceString(ERROR_SDK_IS_NOT_INITIALIZED), null))
            return
        }
        var loginSession = LoginSession()
        loginSession.codeVerifierInternal = utility.generateCodeVerifier()
        loginSession.stateInternal = utility.getRandomString()
        loginSession.nonceInternal = utility.getRandomString()
        loginSession.codeChallenge = utility.generateCodeChallenge(loginSession.codeVerifier)

        // retrieve all the partner info from Android manifest and validate we have value for clientId, redirectUri and scope
        var clientId = utility.getPartnerInfo(applicationContext, PARTNER_CLIENT_ID_ATTRIBUTE)
        if (clientId == null || clientId.isEmpty()) {
            callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.LOADLOGINSESSION, GrabIdPartnerErrorCode.invalidClientId, utility.readResourceString(ERROR_MISSING_CLIENT_ID), null))
            return
        } else {
            loginSession.clientId = clientId
        }

        var redirectUri = utility.getPartnerInfo(applicationContext, PARTNER_REDIRECT_URI_ATTRIBUTE)
        if (redirectUri == null || redirectUri.isEmpty()) {
            callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.LOADLOGINSESSION, GrabIdPartnerErrorCode.invalidRedirectURI, utility.readResourceString(ERROR_MISSING_REDIRECT_URI), null))
            return
        } else {
            loginSession.redirectUri = redirectUri
        }

        var serviceDiscoveryUrl = utility.getPartnerInfo(applicationContext, PARTNER_SERVICE_DISCOVERY_URL)
        if (serviceDiscoveryUrl == null || serviceDiscoveryUrl.isEmpty()) {
            callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.LOADLOGINSESSION, GrabIdPartnerErrorCode.invalidDiscoveryEndpoint, utility.readResourceString(ERROR_MISSING_DISCOVERY_ENDPOINT), null))
            return
        } else {
            loginSession.serviceDiscoveryUrl = serviceDiscoveryUrl
        }

        var scope = utility.getPartnerInfo(applicationContext, PARTNER_SCOPE_ATTRIBUTE)
        if (scope == null || scope.isEmpty()) {
            callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.LOADLOGINSESSION, GrabIdPartnerErrorCode.invalidPartnerScope, utility.readResourceString(ERROR_MISSING_PARTNER_SCOPE), null))
            return
        } else {
            loginSession.scope = scope
        }

        loginSession.acrValues = utility.getPartnerInfo(applicationContext, PARTNER_ACR_VALUES_ATTRIBUTE) ?: ""
        loginSession.request = utility.getPartnerInfo(applicationContext, PARTNER_REQUEST_VALUES_ATTRIBUTE) ?: ""
        loginSession.loginHint = utility.getPartnerInfo(applicationContext, PARTNER_LOGINHINT_VALUES_ATTRIBUTE) ?: ""

        callback.onSuccess(loginSession)
    }

    /**
     * Login user using Grab ID Partner SDK
     */
    override fun login(loginSession: LoginSession, context: Context, callback: LoginCallback) {
        // validate if loginSession has all the required parameters
        if (loginSession.clientId.isNullOrBlank()) {
            callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.LOGIN, GrabIdPartnerErrorCode.invalidClientId, utility.readResourceString(ERROR_MISSING_CLIENT_ID), null))
            return
        }
        if (loginSession.redirectUri.isNullOrBlank()) {
            callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.LOGIN, GrabIdPartnerErrorCode.invalidRedirectURI, utility.readResourceString(ERROR_MISSING_REDIRECT_URI), null))
            return
        }
        if (loginSession.scope.isNullOrBlank()) {
            callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.LOGIN, GrabIdPartnerErrorCode.invalidPartnerScope, utility.readResourceString(ERROR_MISSING_PARTNER_SCOPE), null))
            return
        }
        if (loginSession.serviceDiscoveryUrl.isNullOrBlank()) {
            callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.LOGIN, GrabIdPartnerErrorCode.invalidDiscoveryEndpoint, utility.readResourceString(ERROR_MISSING_DISCOVERY_ENDPOINT), null))
            return
        }

        // check if user has previous valid access token in shared preference
        var loginSessionFromCache: LoginSession? = null
        try {
            loginSessionFromCache = retrieveLoginSessionFromCache(loginSession)
        } catch (exception: Exception) {
            // log the exception and let user continue the login flow
            val sw = StringWriter()
            exception.printStackTrace(PrintWriter(sw))
            LogUtils.debug("login", sw.toString())
        }

        if (loginSessionFromCache != null) {
            utility.cloneLoginSession(loginSessionFromCache, loginSession)
            callback.onSuccess()
            return
        }

        // call the discovery endpoint to fetch all the endpoints
        callDiscovery(context, loginSession, callback)
    }

    /**
    Get the access token from token exchange endpoint
     */
    override fun exchangeToken(loginSession: LoginSession, redirectUrl: String, callback: ExchangeTokenCallback) {
        // validate redirectUrl is not empty or null
        if (redirectUrl.isNullOrBlank()) {
            callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.EXCHANGETOKEN, GrabIdPartnerErrorCode.invalidRedirectURI, utility.readResourceString(ERROR_MISSING_REDIRECT_URI), null))
            return
        }

        // obtain the code and state from the redirect uri
        var code = utility.getURLParam(RESPONSE_TYPE, redirectUrl)
        var state = utility.getURLParam(RESPONSE_STATE, redirectUrl)

        // validate code is not null or empty string
        if (code == null || code.isEmpty()) {
            callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.EXCHANGETOKEN, GrabIdPartnerErrorCode.invalidCode, utility.readResourceString(ERROR_MISSING_CODE), null))
            return
        }

        // check state match with loginSession state
        if (state != loginSession.state) {
            callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.EXCHANGETOKEN, GrabIdPartnerErrorCode.stateMismatch, utility.readResourceString(ERROR_STATE_MISMATCH), null))
            return
        }

        // check if we have valid token endpoint inside loginSession otherwise throw error
        var tokenEndPoint = loginSession.tokenEndpoint
        if (tokenEndPoint == null || tokenEndPoint.isEmpty()) {
            callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.EXCHANGETOKEN, GrabIdPartnerErrorCode.errorInLoginSessionObject, utility.readResourceString(ERROR_MISSING_TOKEN_ENDPOINT_IN_LOGINSESSION), null))
            return
        }

        // save the code and state in the loginSession object
        loginSession.codeInternal = code
        loginSession.stateInternal = state

        grabAuthRepository.getToken(tokenEndPoint, TokenRequest(code, loginSession.clientId, GRANT_TYPE, loginSession.redirectUri, loginSession.codeVerifier))
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe({ result ->
                    // validate that response from the exchange token endpoint has required parameters
                    if (result == null) {
                        callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.EXCHANGETOKEN, GrabIdPartnerErrorCode.errorInExchangeTokenEndpoint, utility.readResourceString(ERROR_NULL_EXCHANGE_TOKEN_RESPONSE), null))
                        return@subscribe
                    }

                    var accesstoken = result.access_token
                    if (accesstoken != null && !accesstoken.isBlank()) {
                        // update the loginSession object
                        loginSession.accessTokenInternal = accesstoken
                    } else {
                        callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.EXCHANGETOKEN, GrabIdPartnerErrorCode.missingAccessToken, utility.readResourceString(ERROR_MISSING_ACCESS_TOKEN), null))
                        return@subscribe
                    }

                    var idToken = result.id_token
                    if (idToken != null && !idToken.isBlank()) {
                        // update the loginSession object
                        loginSession.idTokenInternal = idToken
                    } else {
                        callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.EXCHANGETOKEN, GrabIdPartnerErrorCode.missingIdToken, utility.readResourceString(ERROR_MISSING_ID_TOKEN), null))
                        return@subscribe
                    }

                    var expiresIn = result.expires_in
                    if (expiresIn != null && !expiresIn.isBlank()) {
                        loginSession.accessTokenExpiresAtInternal = utility.addSecondsToCurrentDate(expiresIn)
                    } else {
                        callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.EXCHANGETOKEN, GrabIdPartnerErrorCode.missingAccessTokenExpiry, utility.readResourceString(ERROR_MISSING_ACCESS_TOKEN_EXPIRY), null))
                        return@subscribe
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
                    var gson = Gson()
                    var serializedLoginSession = gson.toJson(loginSession)

                    // encrypt entire loginSession object
                    var encryptedString: String? = null
                    try {
                        val keyPair = androidKeyStoreWrapper.createAndroidKeyStoreAsymmetricKey(utility.generateKeystoreAlias(loginSession, ObjectType.LOGIN_SESSION))
                        encryptedString = cipherWrapper.encrypt(serializedLoginSession, keyPair.public)
                    } catch (exception: Exception) {
                        // log the exception and let user continue the login flow
                        val sw = StringWriter()
                        exception.printStackTrace(PrintWriter(sw))
                        LogUtils.debug("login", sw.toString())
                    }

                    // save loginSession to shared preference
                    if (encryptedString != null) {
                        utility.saveObjectsToSharedPref(loginSession, encryptedString, sharedPreferences, ObjectType.LOGIN_SESSION)
                    }
                    callback.onSuccess()
                    return@subscribe
                }, { error ->
                    var errorJsonString = ""
                    if (error is HttpException) {
                        errorJsonString = error.response().errorBody()?.string() ?: ""
                    }
                    callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.EXCHANGETOKEN, GrabIdPartnerErrorCode.network, errorJsonString, error))
                    return@subscribe
                })
    }

    /**
     * Validate received token is valid using token info endpoint
     */
    override fun getIdTokenInfo(loginSession: LoginSession, callback: GetIdTokenInfoCallback) {
        var idTokenVerificationEndpoint = loginSession.idTokenVerificationEndpoint
        if (idTokenVerificationEndpoint.isNullOrBlank()) {
            callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.GETIDTOKENINFO, GrabIdPartnerErrorCode.errorInLoginSessionObject, utility.readResourceString(ERROR_MISSING_ID_TOKEN_VERIFICATION_ENDPOINT_IN_LOGINSESSION), null))
            return
        }

        // check if user has valid idTokenInfo in the shared preference
        var idTokenInfoFromSharedPref: IdTokenInfo?

        try {
            idTokenInfoFromSharedPref = retrieveIdTokenInfoFromCache(loginSession)
        } catch (ex: Exception) {
            callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.GETIDTOKENINFO, GrabIdPartnerErrorCode.errorRetrievingObjectFromSharedPref, ex.localizedMessage, null))
            return
        }

        if (idTokenInfoFromSharedPref != null) {
            callback.onSuccess(idTokenInfoFromSharedPref)
            return
        }

        grabAuthRepository.getIdTokenInfo(idTokenVerificationEndpoint, loginSession.clientId, loginSession.idToken, loginSession.nonce)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe({
                    // validate nonce is matching with the one received from server
                    if (loginSession.nonce != it.nonce) {
                        callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.GETIDTOKENINFO, GrabIdPartnerErrorCode.errorInGetIdTokenInfo, utility.readResourceString(ERROR_NONCE_MISMATCH_GET_ID_TOKEN_INFO), null))
                        return@subscribe
                    }

                    // serialize idTokenInfo object
                    var gson = Gson()
                    var serializedIdTokenInfo = gson.toJson(it)

                    // encrypt entire idTokenInfo object, Here we intentionally skipping the error callback if any exception occur. For any exception we still wants to send the token info to user and will skip the caching part.
                    var encryptedString: String? = null
                    try {
                        val keyPair = androidKeyStoreWrapper.createAndroidKeyStoreAsymmetricKey(utility.generateKeystoreAlias(loginSession, ObjectType.ID_TOKEN_INFO))
                        encryptedString = cipherWrapper.encrypt(serializedIdTokenInfo, keyPair.public)
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
                    callback.onSuccess(it)
                    return@subscribe
                }, { error ->
                    var errorJsonString = ""
                    if (error is HttpException) {
                        errorJsonString = error.response().errorBody()?.string() ?: ""
                    }
                    callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.GETIDTOKENINFO, GrabIdPartnerErrorCode.errorInGetIdTokenInfo, errorJsonString, error))
                    return@subscribe
                })
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
     * Validate if access token is still valid
     */
    override fun isValidAccessToken(loginSession: LoginSession): Boolean {
        var accessTokenExpireAt = loginSession.accessTokenExpiresAt
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

        var idTokenExpireAt = idTokenInfo.expiration
        var idTokenNotValidBefore = idTokenInfo.notValidBefore
        var currentTimeInUTC = utility.getCurrentTimeInUTC()

        if (idTokenExpireAt == null || idTokenNotValidBefore == null || idTokenExpireAt < currentTimeInUTC || idTokenNotValidBefore > currentTimeInUTC) {
            return false
        }

        return true
    }

    /**
     * Call the discovery endpoint to get the URLs
     */
    private fun callDiscovery(context: Context, loginSession: LoginSession, callback: LoginCallback) {
        grabAuthRepository.callDiscovery(loginSession.serviceDiscoveryUrl)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe({ result ->
                    // validate that response from the discovery endpoint has required parameters
                    if (result == null) {
                        callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.SERVICEDISCOVERY, GrabIdPartnerErrorCode.errorInDiscoveryEndpoint, utility.readResourceString(ERROR_NULL_DISCOVERY_ENDPOINT_RESPONSE), null))
                        return@subscribe
                    }
                    if (!result.authorization_endpoint.isNullOrBlank()) {
                        loginSession.authorizationEndpoint = result.authorization_endpoint
                    } else {
                        callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.SERVICEDISCOVERY, GrabIdPartnerErrorCode.errorInDiscoveryEndpoint, utility.readResourceString(ERROR_MISSING_AUTHORIZE_ENDPOINT_IN_DISCOVERY_ENDPOINT), null))
                        return@subscribe
                    }

                    if (!result.token_endpoint.isNullOrBlank()) {
                        loginSession.tokenEndpoint = result.token_endpoint
                    } else {
                        callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.SERVICEDISCOVERY, GrabIdPartnerErrorCode.errorInDiscoveryEndpoint, utility.readResourceString(ERROR_MISSING_TOKEN_ENDPOINT_IN_DISCOVERY_ENDPOINT), null))
                        return@subscribe
                    }

                    if (!result.id_token_verification_endpoint.isNullOrBlank()) {
                        loginSession.idTokenVerificationEndpoint = result.id_token_verification_endpoint
                    } else {
                        callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.SERVICEDISCOVERY, GrabIdPartnerErrorCode.errorInDiscoveryEndpoint, utility.readResourceString(ERROR_MISSING_VERIFY_TOKEN_ENDPOINT_IN_DISCOVERY_ENDPOINT), null))
                        return@subscribe
                    }
                    chromeCustomTab.openChromeCustomTab(context, loginSession, callback)
                }, { error ->
                    var errorJsonString = ""
                    if (error is HttpException) {
                        errorJsonString = error.response().errorBody()?.string() ?: ""
                    }
                    callback.onError(GrabIdPartnerError(GrabIdPartnerErrorDomain.SERVICEDISCOVERY, GrabIdPartnerErrorCode.errorInDiscoveryEndpoint, errorJsonString, error))
                    return@subscribe
                })
    }

    /**
     * Retrieve the loginSession from shared preference if exist, then decrypt the encrypted string using key store keys
     */
    private fun retrieveLoginSessionFromCache(loginSession: LoginSession): LoginSession? {
        // check if we have valid token for this clientId and scope
        var encryptionData = utility.retrieveObjectFromSharedPref(loginSession, sharedPreferences, ObjectType.LOGIN_SESSION)
        if (encryptionData != null) {
            // decrypt the loginSessionEncrypted data
            var loginSessionDecryptedString: String?
            try {
                var keyPair = androidKeyStoreWrapper.getAndroidKeyStoreAsymmetricKeyPair(utility.generateKeystoreAlias(loginSession, ObjectType.LOGIN_SESSION))
                if (keyPair == null) {
                    deleteEntriesFromSharedPreferenceAndKeystore(loginSession, sharedPreferences)
                    return null
                }
                loginSessionDecryptedString = cipherWrapper.decrypt(encryptionData, keyPair.private)
            } catch (exception: Exception) {
                // delete this entry from shared preferences and also delete the Android keystore keys, no use of keeping that as
                // we not able to decrypt the encrypted string, this is to avoid any future failure
                deleteEntriesFromSharedPreferenceAndKeystore(loginSession, sharedPreferences)

                // write the error in debug log
                val sw = StringWriter()
                exception.printStackTrace(PrintWriter(sw))
                LogUtils.debug("retrieveLoginSessionFromCache", sw.toString())
                return null
            }

            // deserialize to LoginSession object
            var loginSessionFromCache = loginSessionDecryptedString.let { utility.serializeToLoginSession(it) }

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
                    return null

                } else {
                    return loginSessionFromCache
                }

            } else
                return null
        } else
            return null
    }

    /**
     * Retrieve the idTokenInfo from shared preference if exist, then decrypt the encrypted string using key store keys
     */
    private fun retrieveIdTokenInfoFromCache(loginSession: LoginSession): IdTokenInfo? {
        // check if we have valid IdTokenInfo for this clientId and scope
        var encryptionData = utility.retrieveObjectFromSharedPref(loginSession, sharedPreferences, ObjectType.ID_TOKEN_INFO)
        if (encryptionData != null) {
            // decrypt the idTokenInfoEncryptedData data
            var idTokenInfoDecryptedString: String?
            try {
                var keyPair = androidKeyStoreWrapper.getAndroidKeyStoreAsymmetricKeyPair(utility.generateKeystoreAlias(loginSession, ObjectType.ID_TOKEN_INFO))
                if (keyPair == null) {
                    deleteEntriesFromSharedPreferenceAndKeystore(loginSession, sharedPreferences)
                    return null
                }

                idTokenInfoDecryptedString = cipherWrapper.decrypt(encryptionData, keyPair.private)
            } catch (exception: Exception) {
                // delete this entry from shared preferences and also delete the Android keystore keys, no use of keeping that as
                // we not able to decrypt the encrypted string, this is to avoid any future failure
                deleteEntriesFromSharedPreferenceAndKeystore(loginSession, sharedPreferences)

                // write the error in debug log
                val sw = StringWriter()
                exception.printStackTrace(PrintWriter(sw))
                LogUtils.debug("retrieveIdTokenInfoFromCache", sw.toString())
                return null
            }

            // deserialize to LoginSession object
            var idTokenInfo = idTokenInfoDecryptedString.let { utility.serializeToIdTokenInfo(it) }

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
                    return null
                } else {
                    return idTokenInfo
                }

            } else
                return null
        } else
            return null
    }

    /**
     * Method to delete all the entries from shared preference and keystore
     */
    private fun deleteEntriesFromSharedPreferenceAndKeystore(loginSession: LoginSession, sharedPreferences: SharedPreferences) {
        utility.deleteObjectsFromSharedPref(loginSession, sharedPreferences, ObjectType.LOGIN_SESSION)
        utility.deleteObjectsFromSharedPref(loginSession, sharedPreferences, ObjectType.ID_TOKEN_INFO)
        androidKeyStoreWrapper.deleteKeys()

        // remove the token information from loginSession object
        loginSession.accessTokenInternal = ""
        loginSession.idTokenInternal = ""
        loginSession.refreshTokenInternal = ""
        loginSession.accessTokenExpiresAtInternal = null
    }
}

interface LoginSessionCallback {
    fun onSuccess(loginSession: LoginSession)
    fun onError(grabIdPartnerError: GrabIdPartnerError)
}

interface LoginCallback {
    fun onSuccess()
    fun onError(grabIdPartnerError: GrabIdPartnerError)
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