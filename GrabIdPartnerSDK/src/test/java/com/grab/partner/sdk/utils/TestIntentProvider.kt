package com.grab.partner.sdk.utils

import android.content.Intent
import com.nhaarman.mockitokotlin2.mock

/**
 * Test intent provider for verifying intent param or calling.
 */
internal class TestIntentProvider(val mockIntent: Intent = mock(), val mockImpl: IntentProvider = mock()) :
        IntentProvider {
    override fun provideIntent(action: String): Intent {
        mockImpl.provideIntent(action)
        return mockIntent
    }
}