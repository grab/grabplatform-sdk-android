package com.grab.partner.sdk.utils

import android.content.Intent

interface IntentProvider {
    fun provideIntent(action: String) : Intent
}

class IntentProviderImpl : IntentProvider {
    override fun provideIntent(action: String): Intent {
        return Intent(action)
    }
}
