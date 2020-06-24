/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk.sampleappkotlin.wrapper.views

import androidx.databinding.DataBindingUtil
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.grab.partner.sdk.sampleappkotlin.wrapper.R
import com.grab.partner.sdk.sampleappkotlin.wrapper.databinding.ActivityMainBinding
import com.grab.partner.sdk.sampleappkotlin.wrapper.viewmodel.MainActivityViewModel
import javax.inject.Inject
import com.grab.partner.sdk.sampleappkotlin.wrapper.di.components.DaggerSampleAppComponent
import com.grab.partner.sdk.sampleappkotlin.wrapper.di.modules.SampleAppModule

class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var mainActivityViewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding =
            DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        setUpDI()
        binding.vm = mainActivityViewModel
    }

    private fun setUpDI() {
        val component = DaggerSampleAppComponent.builder()
                .sampleAppModule(SampleAppModule(this))
                .build()
        component.inject(this)
    }
}