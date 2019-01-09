/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
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