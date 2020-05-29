/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */
package com.grab.partner.sdk.utils

import android.content.Intent

/**
 * Simple class that allows testability of intent providing
 */
internal interface IntentProvider {

    /**
     * will provide intent with a given action String
     */
    fun provideIntent(action: String) : Intent
}

internal class IntentProviderImpl : IntentProvider {
    override fun provideIntent(action: String): Intent {
        return Intent(action)
    }
}
