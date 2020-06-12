package com.grab.partner.sdk.wrapper.deeplink

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.grab.partner.sdk.GrabIdPartner
import com.grab.partner.sdk.wrapper.chrometabmanager.HAVE_LAUNCHED_CHROME_TAB_MANAGER_ACTIVITY
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
            // if ChromeManagerActivity launched in this login flow then launch it again to clear chrome tab from the back stack
            launchChromeManagerActivity()
            // return result
            GrabSdkManagerImpl.getInstance().returnResult(redirectUrl)
            finish()
        }
    }

    /**
     * check HAVE_LAUNCHED_CHROME_TAB_MANAGER_ACTIVITY variable, if ChromeManagerActivity launched during this login flow then this
     * will be true and we want to relaunch ChromeManagerActivity
     */
    private fun launchChromeManagerActivity() {
        if (HAVE_LAUNCHED_CHROME_TAB_MANAGER_ACTIVITY) {
            val chromeManagerActivityLauncher =
                GrabIdPartner.mainComponent.getChromeManagerActivityLauncher()
            chromeManagerActivityLauncher.launchChromeManagerActivity(this)
            // reset HAVE_LAUNCHED_CHROME_TAB_MANAGER_ACTIVITY as if we're here meaning we completed the OAuth flow
            HAVE_LAUNCHED_CHROME_TAB_MANAGER_ACTIVITY = false
        }
    }
}