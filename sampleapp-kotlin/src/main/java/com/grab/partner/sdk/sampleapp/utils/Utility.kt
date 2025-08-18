/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk.sampleapp.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

class Utility {
    companion object {
        /**
         * Utility method to read metadata entry from Android Manifest file
         */
        fun readMetadata(context: Context, attribute: String): String? {
            val packageManager = context.packageManager ?: return null
            val applicationInfo: ApplicationInfo?
            try {
                applicationInfo = packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            } catch (e: PackageManager.NameNotFoundException) {
                return null
            }

            if (applicationInfo.metaData == null) {
                return null
            }

            return applicationInfo.metaData.get(attribute)?.toString()
        }

        /**
         * Utility method to save a key in Android SharedPreference instance
         */
        fun saveToSharedPreference(sharedPreferences: SharedPreferences, keyName: String, keyValue: String) {
            var sharedPreferenceEditor = sharedPreferences.edit()
            sharedPreferenceEditor.putString(keyName, keyValue)
            sharedPreferenceEditor.apply()
        }
    }
}