package com.grab.partner.sdk.utils

import android.content.Intent
import com.nhaarman.mockitokotlin2.mock

class TestIntentProvider(val mockIntent: Intent = mock(), val mockImpl: IntentProvider = mock()) :
        IntentProvider {
    override fun provideIntent(action: String): Intent {
        mockImpl.provideIntent(action)
        return mockIntent
    }
}