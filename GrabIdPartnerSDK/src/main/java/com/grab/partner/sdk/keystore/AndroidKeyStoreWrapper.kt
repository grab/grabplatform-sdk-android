/*
 * Copyright (c) 2012-2019 Grab Taxi Holdings PTE LTD (GRAB), All Rights Reserved. NOTICE: All information contained herein is, and remains the property of GRAB. The intellectual and technical concepts contained herein are confidential, proprietary and controlled by GRAB and may be covered by patents, patents in process, and are protected by trade secret or copyright law.
 * You are strictly forbidden to copy, download, store (in any medium), transmit, disseminate, adapt or change this material in any way unless prior written permission is obtained from GRAB. Access to the source code contained herein is hereby forbidden to anyone except current GRAB employees or contractors with binding Confidentiality and Non-disclosure agreements explicitly covering such access.
 *
 * The copyright notice above does not evidence any actual or intended publication or disclosure of this source code, which includes information that is confidential and/or proprietary, and is a trade secret, of GRAB.
 * ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC PERFORMANCE, OR PUBLIC DISPLAY OF OR THROUGH USE OF THIS SOURCE CODE WITHOUT THE EXPRESS WRITTEN CONSENT OF GRAB IS STRICTLY PROHIBITED, AND IN VIOLATION OF APPLICABLE LAWS AND INTERNATIONAL TREATIES. THE RECEIPT OR POSSESSION OF THIS SOURCE CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY ANY RIGHTS TO REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR SELL ANYTHING THAT IT MAY DESCRIBE, IN WHOLE OR IN PART.
 */

package com.grab.partner.sdk.keystore

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.util.Calendar
import javax.security.auth.x500.X500Principal

internal interface IAndroidKeyStoreWrapper {
    /**
     * Get AsymmetricKeyPair for a given alias
     */
    fun getAndroidKeyStoreAsymmetricKeyPair(alias: String): KeyPair?

    /**
     * Generate AsymmetricKeyPair for a given alias
     */
    fun createAndroidKeyStoreAsymmetricKey(alias: String): KeyPair

    /**
     * Delete all the keystore keys
     */
    fun deleteKeys(): Boolean
}

internal class AndroidKeyStoreWrapper(private val context: Context) : IAndroidKeyStoreWrapper {
    companion object {
        val ANDROID_KEY_STORE = "AndroidKeyStore"
        val ENCRYPTION_ALGORITHM = "RSA"
    }

    private val keyStore: KeyStore = createAndroidKeyStore()

    /**
     * Get AsymmetricKeyPair for a given alias
     */
    override fun getAndroidKeyStoreAsymmetricKeyPair(alias: String): KeyPair? {
        val privateKey = keyStore.getKey(alias, null) as PrivateKey?
        val publicKey = keyStore.getCertificate(alias)?.publicKey

        return if (privateKey != null && publicKey != null) {
            KeyPair(publicKey, privateKey)
        } else {
            null
        }
    }

    /**
     * Generate AsymmetricKeyPair for a given alias
     */
    override fun createAndroidKeyStoreAsymmetricKey(alias: String): KeyPair {
        val generator = KeyPairGenerator.getInstance(ENCRYPTION_ALGORITHM, ANDROID_KEY_STORE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            initKeyGenParameterSpec(generator, alias)
        } else {
            initKeyPairGeneratorSpec(generator, alias)
        }

        return generator.generateKeyPair()
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private fun initKeyPairGeneratorSpec(generator: KeyPairGenerator, alias: String) {
        val startDate = Calendar.getInstance()
        val endDate = Calendar.getInstance()
        endDate.add(Calendar.YEAR, 20)

        val builder = KeyPairGeneratorSpec.Builder(context)
                .setAlias(alias)
                .setSerialNumber(BigInteger.ONE)
                .setSubject(X500Principal("CN=${alias} CA Certificate"))
                .setStartDate(startDate.time)
                .setEndDate(endDate.time)

        generator.initialize(builder.build())
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun initKeyGenParameterSpec(generator: KeyPairGenerator, alias: String) {
        val builder = KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
        generator.initialize(builder.build())
    }

    private fun createAndroidKeyStore(): KeyStore {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
        keyStore.load(null)
        return keyStore
    }

    /**
     * Delete all the keystore keys
     */
    override fun deleteKeys(): Boolean {
        // iterate over all the keys and delete one by one
        var aliases = keyStore.aliases()

        aliases?.let {
            for (alias in aliases) {
                try {
                    keyStore.deleteEntry(alias)
                } catch (exception: java.lang.Exception) {
                    return false
                }
            }
        }
        return true
    }
}