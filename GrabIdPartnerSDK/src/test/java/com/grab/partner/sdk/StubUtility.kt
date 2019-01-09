/*
 * Copyright (c) 2012-2019 Grab Taxi Holdings PTE LTD (GRAB), All Rights Reserved. NOTICE: All information contained herein is, and remains the property of GRAB. The intellectual and technical concepts contained herein are confidential, proprietary and controlled by GRAB and may be covered by patents, patents in process, and are protected by trade secret or copyright law.
 * You are strictly forbidden to copy, download, store (in any medium), transmit, disseminate, adapt or change this material in any way unless prior written permission is obtained from GRAB. Access to the source code contained herein is hereby forbidden to anyone except current GRAB employees or contractors with binding Confidentiality and Non-disclosure agreements explicitly covering such access.
 *
 * The copyright notice above does not evidence any actual or intended publication or disclosure of this source code, which includes information that is confidential and/or proprietary, and is a trade secret, of GRAB.
 * ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC PERFORMANCE, OR PUBLIC DISPLAY OF OR THROUGH USE OF THIS SOURCE CODE WITHOUT THE EXPRESS WRITTEN CONSENT OF GRAB IS STRICTLY PROHIBITED, AND IN VIOLATION OF APPLICABLE LAWS AND INTERNATIONAL TREATIES. THE RECEIPT OR POSSESSION OF THIS SOURCE CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY ANY RIGHTS TO REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR SELL ANYTHING THAT IT MAY DESCRIBE, IN WHOLE OR IN PART.
 */

package com.grab.partner.sdk

import android.content.Context
import android.content.SharedPreferences
import com.grab.partner.sdk.models.IdTokenInfo
import com.grab.partner.sdk.models.LoginSession
import com.grab.partner.sdk.utils.IUtility
import com.grab.partner.sdk.utils.ObjectType
import com.grab.partner.sdk.utils.UTC_TIMEZONE
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

    override fun getPartnerInfo(context: Context, attribute: String): String? {
        return partnerInfoMap?.get(attribute)
    }

    fun setPartnerInfo(map: Map<String, String?>) {
        this.partnerInfoMap = map
    }

    override fun readResourceString(mystring: Int): String {
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

    override fun serializeToLoginSession(loginSessionString: String): LoginSession? {
        return null
    }

    override fun serializeToIdTokenInfo(idTokenInfoString: String): IdTokenInfo? {
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
}