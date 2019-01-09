/*
 * Copyright (c) 2012-2019 Grab Taxi Holdings PTE LTD (GRAB), All Rights Reserved. NOTICE: All information contained herein is, and remains the property of GRAB. The intellectual and technical concepts contained herein are confidential, proprietary and controlled by GRAB and may be covered by patents, patents in process, and are protected by trade secret or copyright law.
 * You are strictly forbidden to copy, download, store (in any medium), transmit, disseminate, adapt or change this material in any way unless prior written permission is obtained from GRAB. Access to the source code contained herein is hereby forbidden to anyone except current GRAB employees or contractors with binding Confidentiality and Non-disclosure agreements explicitly covering such access.
 *
 * The copyright notice above does not evidence any actual or intended publication or disclosure of this source code, which includes information that is confidential and/or proprietary, and is a trade secret, of GRAB.
 * ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC PERFORMANCE, OR PUBLIC DISPLAY OF OR THROUGH USE OF THIS SOURCE CODE WITHOUT THE EXPRESS WRITTEN CONSENT OF GRAB IS STRICTLY PROHIBITED, AND IN VIOLATION OF APPLICABLE LAWS AND INTERNATIONAL TREATIES. THE RECEIPT OR POSSESSION OF THIS SOURCE CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY ANY RIGHTS TO REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR SELL ANYTHING THAT IT MAY DESCRIBE, IN WHOLE OR IN PART.
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

            if (applicationInfo?.metaData == null) {
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