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
import android.content.pm.PackageManager
import com.grab.partner.sdk.models.IdTokenInfo
import com.grab.partner.sdk.models.LoginSession
import com.grab.partner.sdk.models.ProtocolInfo
import com.grab.partner.sdk.utils.IUtility
import com.grab.partner.sdk.utils.ObjectType
import com.grab.partner.sdk.utils.UTC_TIMEZONE
import io.reactivex.Maybe
import io.reactivex.Single
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

internal const val CONST_READ_RESOURCE_STRING = "stub_message"
internal const val CONST_RANDOM_STRING = "random_string"
internal const val CONST_CODE_VERIFIER_STRING = "code_verifier_string"
internal const val CONST_CODE_CHALLENGE_STRING = "code_challenge_string"
internal const val CONST_KEYSTORE_ALIAS = "fake_keystore_alias"

class StubUtility : IUtility {
    private var getURLParamMap: Map<String, String>? = null
    private var partnerInfoMap: Map<String, String?>? = null
    private var objectFromSharedPreferences: String? = null
    private var idTokenInfo: IdTokenInfo? = null
    private var protocolInfo: ProtocolInfo? = null

    override fun getPartnerInfo(context: Context?, attribute: String): String? {
        return partnerInfoMap?.get(attribute)
    }

    fun setPartnerInfo(map: Map<String, String?>) {
        this.partnerInfoMap = map
    }

    override fun readResourceString(context: Context?, mystring: Int): String {
        return CONST_READ_RESOURCE_STRING
    }

    override fun getURLParam(param: String, url: String): String? {
        return getURLParamMap?.get(param)
    }

    fun setURLParamReturn(map: Map<String, String>) {
        this.getURLParamMap = map
    }

    override fun getRandomString(): String {
        return CONST_RANDOM_STRING
    }

    override fun generateCodeVerifier(): String {
        return CONST_CODE_VERIFIER_STRING
    }

    override fun generateCodeChallenge(code_verifier: String): String {
        return CONST_CODE_CHALLENGE_STRING
    }

    override fun addSecondsToCurrentDate(seconds: String): Date? {
        return Date()
    }

    fun addDaysToCurrentDate(days: Int): Date {
        val utcTimeZone = TimeZone.getTimeZone(UTC_TIMEZONE)
        TimeZone.setDefault(utcTimeZone)
        val calendar = Calendar.getInstance(utcTimeZone)
        calendar.add(Calendar.HOUR, days * 24)
        return calendar.time
    }

    fun subtractDaysFromCurrentDate(days: Int): Date {
        val utcTimeZone = TimeZone.getTimeZone(UTC_TIMEZONE)
        TimeZone.setDefault(utcTimeZone)
        val calendar = Calendar.getInstance(utcTimeZone)
        calendar.add(Calendar.HOUR, (-1) * days * 24)
        return calendar.time
    }

    override fun getCurrentTimeInUTC(): Date {
        val utcTimeZone = TimeZone.getTimeZone(UTC_TIMEZONE)
        TimeZone.setDefault(utcTimeZone)
        val calendar = Calendar.getInstance(utcTimeZone)

        return calendar.time
    }

    override fun generateKeystoreAlias(loginSession: LoginSession, enum: Enum<ObjectType>): String {
        return CONST_KEYSTORE_ALIAS
    }

    override fun sortedScopeString(scope: String): String {
        return ""
    }

    override fun saveObjectsToSharedPref(loginSession: LoginSession, encryptionData: String, sharedPreferences: SharedPreferences, objectType: ObjectType) {
        this.objectFromSharedPreferences = encryptionData
    }

    override fun retrieveObjectFromSharedPref(loginSession: LoginSession, sharedPreferences: SharedPreferences, objectType: ObjectType): String? {
        return ""
    }

    fun setObjectToSharedPref(value: String?) {
        this.objectFromSharedPreferences = value
    }

    override fun deSerializeToLoginSession(loginSessionString: String): LoginSession? {
        return null
    }

    override fun deSerializeToIdTokenInfo(idTokenInfoString: String): IdTokenInfo? {
        return this.idTokenInfo
    }

    fun setSerializedIdTokenInfo(idTokenInfo: IdTokenInfo) {
        this.idTokenInfo = idTokenInfo
    }

    override fun deleteObjectsFromSharedPref(loginSession: LoginSession, sharedPreferences: SharedPreferences, objectType: ObjectType) {
    }

    override fun cloneLoginSession(source: LoginSession, destination: LoginSession) {
    }

    override fun clearLoginSession(loginSession: LoginSession) {
    }

    override fun isPackageInstalled(protocols: List<String>?, packageManager: PackageManager): Maybe<ProtocolInfo> {
        if (protocolInfo == null)
            return Maybe.empty()

        return Maybe.just(protocolInfo)
    }

    fun setIsPackageInstalled(protocolInfo: ProtocolInfo?) {
        this.protocolInfo = protocolInfo
    }
}