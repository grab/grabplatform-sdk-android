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
