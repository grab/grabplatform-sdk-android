package com.grab.partner.sdk.wrapper.deeplink

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log

import com.grab.partner.sdk.wrapper.manager.GrabSdkManager
import com.grab.partner.sdk.wrapper.manager.GrabSdkManagerImpl

import javax.inject.Inject

class DeepLinkActivity : AppCompatActivity() {

    @Inject lateinit var manager: GrabSdkManagerImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        GrabSdkManagerImpl.component
                .inject(this)

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
            manager.returnResult(redirectUrl)
            finish()
        }
    }
}
