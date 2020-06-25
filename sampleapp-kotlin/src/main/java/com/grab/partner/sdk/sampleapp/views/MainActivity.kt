/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk.sampleapp.views

import android.content.Intent
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.grab.partner.sdk.sampleapp.databinding.ActivityMainBinding
import com.grab.partner.sdk.sampleapp.viewmodel.MainActivityViewModel
import javax.inject.Inject
import com.grab.partner.sdk.sampleapp.R
import com.grab.partner.sdk.sampleapp.di.components.DaggerSampleAppComponent
import com.grab.partner.sdk.sampleapp.di.modules.SampleAppModule

class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var mainActivityViewModel: MainActivityViewModel
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setUpDI()
        binding.vm = mainActivityViewModel
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        var action = intent.action
        var redirectUrl = intent.dataString
        if (Intent.ACTION_VIEW == action && redirectUrl != null) {
            mainActivityViewModel.setRedirectUrl(redirectUrl)
            // initiate the token exchange with GRAB ID Partner SDK
            mainActivityViewModel.getToken()
        }
    }

    private fun setUpDI() {
        val component = DaggerSampleAppComponent.builder()
            .sampleAppModule(SampleAppModule(this))
            .build()
        component.inject(this)
    }
}