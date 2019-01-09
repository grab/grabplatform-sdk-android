/*
 * Copyright (c) 2012-2019 Grab Taxi Holdings PTE LTD (GRAB), All Rights Reserved. NOTICE: All information contained herein is, and remains the property of GRAB. The intellectual and technical concepts contained herein are confidential, proprietary and controlled by GRAB and may be covered by patents, patents in process, and are protected by trade secret or copyright law.
 * You are strictly forbidden to copy, download, store (in any medium), transmit, disseminate, adapt or change this material in any way unless prior written permission is obtained from GRAB. Access to the source code contained herein is hereby forbidden to anyone except current GRAB employees or contractors with binding Confidentiality and Non-disclosure agreements explicitly covering such access.
 *
 * The copyright notice above does not evidence any actual or intended publication or disclosure of this source code, which includes information that is confidential and/or proprietary, and is a trade secret, of GRAB.
 * ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC PERFORMANCE, OR PUBLIC DISPLAY OF OR THROUGH USE OF THIS SOURCE CODE WITHOUT THE EXPRESS WRITTEN CONSENT OF GRAB IS STRICTLY PROHIBITED, AND IN VIOLATION OF APPLICABLE LAWS AND INTERNATIONAL TREATIES. THE RECEIPT OR POSSESSION OF THIS SOURCE CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY ANY RIGHTS TO REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR SELL ANYTHING THAT IT MAY DESCRIBE, IN WHOLE OR IN PART.
 */

package com.grab.partner.sdk.keystore

import android.util.Base64
import java.security.Key
import java.util.Arrays
import javax.crypto.Cipher

internal interface ICipher {
    /**
     * Encrypt a string using the public key
     */
    fun encrypt(data: String, key: Key?): String

    /**
     * Decrypt a string using the private key
     */
    fun decrypt(data: String, key: Key?): String

    /**
     * Encrypt the long string in stages by splitting it into smaller strings
     */
    fun encryptInStages(data: String): String
}

internal class CipherWrapper : ICipher {
    companion object {
        var TRANSFORMATION_ASYMMETRIC = "RSA/ECB/PKCS1Padding"
        val CHARSET = "UTF-8"
    }

    private val cipher: Cipher = Cipher.getInstance(TRANSFORMATION_ASYMMETRIC)

    /**
     * Encrypt a string using the public key
     */
    override fun encrypt(data: String, key: Key?): String {
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return encryptInStages(data)
    }

    /**
     * Decrypt a string using the private key
     */
    override fun decrypt(data: String, key: Key?): String {
        cipher.init(Cipher.DECRYPT_MODE, key)
        var sb = StringBuilder()
        // split the string by deliminator and the decrypt the entries in the list
        var list = data.split(',')
        for (encodedString in list) {
            val encryptedData = Base64.decode(encodedString, Base64.NO_WRAP)
            val decodedData = String(cipher.doFinal(encryptedData), charset(CHARSET))
            sb.append(decodedData)
        }

        return sb.toString()
    }

    /**
     * Encrypt the long string in stages by splitting it into smaller strings
     */
    override fun encryptInStages(data: String): String {
        var dateInByteArray = data.toByteArray(charset(CHARSET))
        var maxIndex = dateInByteArray.size
        var startIndex = 0
        var endIndex = 120

        var sb = StringBuilder()

        while (true) {
            if (endIndex > maxIndex) {
                endIndex = maxIndex
            }

            var byteArray = Arrays.copyOfRange(dateInByteArray, startIndex, endIndex)
            var bytes = cipher.doFinal(byteArray)
            var string = Base64.encodeToString(bytes, Base64.NO_WRAP)


            //list.add(string)
            if (endIndex == maxIndex) {
                sb.append(string)
                break
            } else {
                sb.append("$string,")
            }
            startIndex = endIndex
            endIndex += 120
        }

        return sb.toString()
    }
}