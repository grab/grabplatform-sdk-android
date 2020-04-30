/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.util.Base64
import com.google.gson.Gson
import com.grab.partner.sdk.GrabIdPartner
import com.grab.partner.sdk.GrabIdPartner.Companion.ENCODING_SETTING
import com.grab.partner.sdk.models.IdTokenInfo
import com.grab.partner.sdk.models.LoginSession
import com.grab.partner.sdk.models.PlaystoreProtocol
import com.grab.partner.sdk.models.ProtocolInfo
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import java.net.URLDecoder
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import java.util.UUID

private const val ENCODING = "UTF-8"
internal const val UTC_TIMEZONE = "UTC"

interface IUtility {
    /**
     * Retrieve partner info which later will be used to verify partner.
     * The info should be registered inside AndroidManifest.xml in order to identify partner.
     *
     * @param context The application context.
     */
    fun getPartnerInfo(context: Context?, attribute: String): String?

    /**
     * Method to check if a specific package is installed in the device
     */
    fun isPackageInstalled(protocols: List<String>?, packageManager: PackageManager): Maybe<ProtocolInfo>

    /**
     * Method to check if specific protocol for launching to backend is available
     */
    fun getPlaystoreString(protocols: List<String>?, packageManager: PackageManager): Single<String>

    /**
     * Retrieve the string values from the string.xml file
     */
    fun readResourceString(context: Context?, mystring: Int): String

    /**
     * Method to read value of any query string parameter
     */
    fun getURLParam(param: String, url: String): String?

    /**
     * Random string generator used for nonce and state
     */
    fun getRandomString(): String

    /**
     * Method to generate code_verifier for OAuth 2.0 PKCE support
     */
    fun generateCodeVerifier(): String

    /**
     * Method to generate code_challenge for OAuth 2.0 PKCE support
     */
    fun generateCodeChallenge(code_verifier: String): String

    /**
     * Method to add seconds to current datetime
     */
    fun addSecondsToCurrentDate(seconds: String): Date?

    fun getCurrentTimeInUTC(): Date
    /**
     * Method to generate the Android Keystore alias for saving different access tokens
     */
    fun generateKeystoreAlias(loginSession: LoginSession, enum: Enum<ObjectType>): String

    /**
     * Method to sort the scope string
     */
    fun sortedScopeString(scope: String): String

    /**
     * Method to save loginSession object inside shared preference
     */
    fun saveObjectsToSharedPref(loginSession: LoginSession, encryptionData: String, sharedPreferences: SharedPreferences, objectType: ObjectType)

    /**
     * Method to retrieve the loginSession object from shared preference
     */
    fun retrieveObjectFromSharedPref(loginSession: LoginSession, sharedPreferences: SharedPreferences, objectType: ObjectType): String?

    /**
     * Deserialize string to LoginSession object
     */
    fun deSerializeToLoginSession(loginSessionString: String): LoginSession?

    /**
     * Deserialize string to IdTokenInfo object
     */
    fun deSerializeToIdTokenInfo(idTokenInfoString: String): IdTokenInfo?

    /**
     * Method to delete the loginSession object from shared preference
     */
    fun deleteObjectsFromSharedPref(loginSession: LoginSession, sharedPreferences: SharedPreferences, objectType: ObjectType)

    /**
     * clone loginSession object to another loginSession object
     */
    fun cloneLoginSession(source: LoginSession, destination: LoginSession)

    /**
     * Clear all properties from LoginSession object
     */
    fun clearLoginSession(loginSession: LoginSession)
}

internal class Utility : IUtility {
    /**
     * Retrieve partner info which later will be used to verify partner.
     * The info should be registered inside AndroidManifest.xml in order to identify partner.
     *
     * @param context The application context.
     */
    override fun getPartnerInfo(context: Context?, attribute: String): String? {
        val packageManager = context?.packageManager ?: return null
        val applicationInfo: ApplicationInfo?
        try {
            applicationInfo = packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        } catch (e: PackageManager.NameNotFoundException) {
            return null
        }

        if (applicationInfo?.metaData == null) {
            return null
        }

        return applicationInfo.metaData.get(attribute)?.toString()
    }

    /**
     * Retrieve the string values from the string.xml file
     */
    override fun readResourceString(context: Context?, mystring: Int): String {
        return try {
            context?.resources?.getString(mystring) ?: ""
        } catch (exception: Resources.NotFoundException) {
            ""
        }
    }

    /**
     * Method to read value of any query string parameter
     */
    override fun getURLParam(param: String, url: String): String? {
        var uri = Uri.parse(url)
        var paramValue = uri.getQueryParameter(param)

        if (paramValue.isNullOrBlank())
            return null

        return URLDecoder.decode(paramValue, ENCODING)
    }

    /**
     * Random string generator used for nonce and state
     */
    override fun getRandomString(): String {
        return UUID.randomUUID().toString()
    }

    /**
     * Method to generate code_verifier for OAuth 2.0 PKCE support
     */
    override fun generateCodeVerifier(): String {
        val sr = SecureRandom()
        val code = ByteArray(32)
        sr.nextBytes(code)
        return Base64.encodeToString(code, ENCODING_SETTING)
    }

    /**
     * Method to generate code_challenge for OAuth 2.0 PKCE support
     */
    override fun generateCodeChallenge(code_verifier: String): String {
        val bytes = code_verifier.toByteArray(Charsets.UTF_8)
        val md = MessageDigest.getInstance(GrabIdPartner.HASH_ALGORITHM)
        md.update(bytes, 0, bytes.size)
        val digest = md.digest()
        return Base64.encodeToString(digest, ENCODING_SETTING)
    }

    /**
     * Method to add seconds to current datetime
     */
    override fun addSecondsToCurrentDate(seconds: String): Date? {
        return try {
            val utcTimeZone = TimeZone.getTimeZone(UTC_TIMEZONE)
            TimeZone.setDefault(utcTimeZone)
            val calendar = Calendar.getInstance(utcTimeZone)
            calendar.add(Calendar.SECOND, seconds.toInt())
            calendar.time
        } catch (exception: NumberFormatException) {
            null
        }
    }

    override fun getCurrentTimeInUTC(): Date {
        val utcTimeZone = TimeZone.getTimeZone(UTC_TIMEZONE)
        TimeZone.setDefault(utcTimeZone)
        val calendar = Calendar.getInstance(utcTimeZone)

        return calendar.time
    }

    /**
     * Method to generate the Android Keystore alias for saving different access tokens
     */
    override fun generateKeystoreAlias(loginSession: LoginSession, enum: Enum<ObjectType>): String {
        var scopeSortedString = sortedScopeString(loginSession.scope)
        return loginSession.clientId + "_" + scopeSortedString + "_" + enum.name
    }

    /**
     * Method to sort the scope string
     */
    override fun sortedScopeString(scope: String): String {
        if (scope.isNullOrBlank()) {
            return ""
        }
        // split the scope with space and then sort the list
        var scopeArray = scope.toLowerCase().split(" ", ignoreCase = true)
        scopeArray = scopeArray.sortedDescending()

        return scopeArray.joinToString("_")
    }

    /**
     * Method to save loginSession object inside shared preference
     */
    override fun saveObjectsToSharedPref(loginSession: LoginSession, encryptionData: String, sharedPreferences: SharedPreferences, objectType: ObjectType) {
        // GSON to serialize encryptionData object to save in shared preference
        var keyName = generateKeystoreAlias(loginSession, objectType)
        var sharedPreferenceEditor = sharedPreferences.edit()
        sharedPreferenceEditor.putString(keyName, encryptionData).apply()
        sharedPreferenceEditor.commit()
    }

    /**
     * Method to retrieve the loginSession object from shared preference
     */
    override fun retrieveObjectFromSharedPref(loginSession: LoginSession, sharedPreferences: SharedPreferences, objectType: ObjectType): String? {
        var keyName = generateKeystoreAlias(loginSession, objectType)
        return sharedPreferences.getString(keyName, null)
    }

    /**
     * Deserialize string to LoginSession object
     */
    override fun deSerializeToLoginSession(loginSessionString: String): LoginSession? {
        var gson = Gson()
        return try {
            gson.fromJson(loginSessionString, LoginSession::class.java)
        } catch (ex: Exception) {
            null
        }
    }

    /**
     * Deserialize string to IdTokenInfo object
     */
    override fun deSerializeToIdTokenInfo(idTokenInfoString: String): IdTokenInfo? {
        var gson = Gson()
        return try {
            gson.fromJson(idTokenInfoString, IdTokenInfo::class.java)
        } catch (ex: Exception) {
            null
        }
    }

    /**
     * Method to delete the loginSession object from shared preference
     */
    override fun deleteObjectsFromSharedPref(loginSession: LoginSession, sharedPreferences: SharedPreferences, objectType: ObjectType) {
        var keyName = generateKeystoreAlias(loginSession, objectType)

        var sharedPreferenceEditor = sharedPreferences.edit()
        sharedPreferenceEditor.remove(keyName)
        sharedPreferenceEditor.apply()
    }

    /**
     * clone loginSession object to another loginSession object
     */
    override fun cloneLoginSession(source: LoginSession, destination: LoginSession) {
        destination.apply {
            accessTokenInternal = source.accessToken
            idTokenInternal = source.idToken
            refreshTokenInternal = source.refreshToken
            accessTokenExpiresAtInternal = source.accessTokenExpiresAtInternal
            clientId = source.clientId
            scope = source.scope
            codeInternal = source.code
            codeVerifierInternal = source.codeVerifier
            stateInternal = source.state
            tokenTypeInternal = source.tokenType
            nonceInternal = source.nonce
            isDebug = source.isDebug
            tokenEndpoint = source.tokenEndpoint
            authorizationEndpoint = source.authorizationEndpoint
            idTokenVerificationEndpoint = source.idTokenVerificationEndpoint
        }
    }

    /**
     * Clear all properties from LoginSession object
     */
    override fun clearLoginSession(loginSession: LoginSession) {
        loginSession.accessTokenInternal = ""
        loginSession.idTokenInternal = ""
        loginSession.refreshTokenInternal = ""
        loginSession.accessTokenExpiresAtInternal = null
        loginSession.clientId = ""
        loginSession.scope = ""
        loginSession.codeInternal = ""
        loginSession.codeVerifierInternal = ""
        loginSession.stateInternal = ""
        loginSession.tokenTypeInternal = ""
        loginSession.nonceInternal = ""
        loginSession.isDebug = false
        loginSession.tokenEndpoint = ""
        loginSession.authorizationEndpoint = ""
        loginSession.idTokenVerificationEndpoint = ""
        loginSession.codeChallenge = ""
        loginSession.redirectUri = ""
    }

    override fun isPackageInstalled(protocols: List<String>?, packageManager: PackageManager): Maybe<ProtocolInfo> {
        var gson = Gson()

        if (protocols != null) {
            for (customProtocol in protocols) {
                // check if the package is installed
                try {
                    var protocolInfo = gson.fromJson(customProtocol, ProtocolInfo::class.java)
                    // if any of the required parameters are missing then no need to proceed
                    if (protocolInfo.minversion_adr.isNullOrEmpty() || protocolInfo.package_adr.isNullOrEmpty() || protocolInfo.protocol_adr.isNullOrEmpty()) {
                        continue
                    }

                    var packageInfo = packageManager.getPackageInfo(protocolInfo.package_adr, 0)
                    if (packageInfo != null) {
                        var installedAppVersion = Version(packageInfo.versionName)
                        var requiredAppVersion = Version(protocolInfo.minversion_adr)

                        if (installedAppVersion >= requiredAppVersion) {
                            return Maybe.just(protocolInfo)
                        }
                    }
                } catch (ex: Exception) {
                    // do nothing
                }
            }
        }
        return Maybe.empty()
    }

    override fun getPlaystoreString(protocols: List<String>?, packageManager: PackageManager): Single<String> {

        return if (protocols.isNullOrEmpty()) {
            Single.just("")
        } else {
            Observable.fromIterable(protocols)
                    .map { mapToPlaystoreLink(it) }
                    .filter(String::isNotEmpty)
                    .firstOrError()
                    .onErrorReturn { "" }
        }
    }

    private fun mapToPlaystoreLink(it: String): String {
        return try {
            val protocolInfo = Gson().fromJson(it, PlaystoreProtocol::class.java)
            protocolInfo.appstore_link_adr ?: ""
            // if any of the required parameters are missing then no need to proceed
        } catch (ex: Exception) {
            ""
        }
    }
}

enum class ObjectType {
    LOGIN_SESSION,
    ID_TOKEN_INFO
}