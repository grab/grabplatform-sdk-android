/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk

import com.grab.partner.sdk.keystore.ICipher
import java.security.Key

internal const val CONST_ENCRYPTED_STRING = "encrypted_string"

class StubCipherWrapper : ICipher {
    private var decrypted_string = ""

    override fun encrypt(data: String, key: Key?): String {
        return CONST_ENCRYPTED_STRING
    }

    override fun decrypt(data: String, key: Key?): String {
        return this.decrypted_string
    }

    fun setDecryptedString(decrypted_string: String){
        this.decrypted_string = decrypted_string
    }

    override fun encryptInStages(data: String): String {
        return ""
    }
}