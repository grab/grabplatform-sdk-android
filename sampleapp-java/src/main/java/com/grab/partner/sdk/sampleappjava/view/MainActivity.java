/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk.sampleappjava.view;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.grab.partner.sdk.sampleappjava.viewmodel.MainActivityViewModel;
import com.grab.partner.sdk.sampleappjava.R;
import com.grab.partner.sdk.sampleappjava.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private MainActivityViewModel viewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        viewModel = new MainActivityViewModel(getApplicationContext(), binding);
        binding.setVm(viewModel);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String action = intent.getAction();
        String redirectUrl = intent.getDataString();
        if (Intent.ACTION_VIEW == action && redirectUrl != null) {
            viewModel.setRedirectUrl(redirectUrl);
            // initiate the token exchange with GRAB ID Partner SDK
            viewModel.getToken();
        }
    }
}
