package com.grab.partner.sdk.wrapper.deeplink

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.grab.partner.sdk.wrapper.manager.GrabSdkManagerImpl

class DeepLinkActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getRedirectUrl(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        getRedirectUrl(intent)
    }

    private fun getRedirectUrl(intent: Intent) {
        val action = intent.action
        val redirectUrl = intent.dataString
        if (Intent.ACTION_VIEW === action && redirectUrl != null) {
            GrabSdkManagerImpl.getInstance().returnResult(redirectUrl)
            finish()
        }
    }
}
