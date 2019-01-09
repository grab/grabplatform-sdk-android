/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk

import com.grab.partner.sdk.keystore.IAndroidKeyStoreWrapper
import java.security.KeyPair

class StubAndroidKeyStoreWrapper(var keyPair: KeyPair) : IAndroidKeyStoreWrapper {
    override fun getAndroidKeyStoreAsymmetricKeyPair(alias: String): KeyPair? {
        return keyPair
    }

    override fun createAndroidKeyStoreAsymmetricKey(alias: String): KeyPair {
        return keyPair
    }

    override fun deleteKeys(): Boolean {
        return false
    }
}