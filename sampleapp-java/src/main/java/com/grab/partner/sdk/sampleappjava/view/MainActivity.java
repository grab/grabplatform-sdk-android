/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk.sampleappjava.view;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.core.view.WindowInsetsCompat;


import com.grab.partner.sdk.sampleappjava.viewmodel.MainActivityViewModel;
import com.grab.partner.sdk.sampleappjava.R;
import com.grab.partner.sdk.sampleappjava.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private MainActivityViewModel viewModel;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
            applySystemBarStyle();
        }
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        viewModel = new MainActivityViewModel(this, binding);
        binding.setVm(viewModel);
        View contentContainer = binding.getRoot() instanceof ViewGroup
                ? ((ViewGroup) binding.getRoot()).getChildAt(0)
                : binding.getRoot();
        setContentTopInset(contentContainer);
        setRootBottomInset(binding.getRoot());
        setActionBarTopInset();
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

    private void setContentTopInset(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            int originPaddingTop = view.getPaddingTop();
            int actionBarHeight = resolveActionBarHeight();

            ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
                Insets systemInsets = windowInsets.getInsets(
                        WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()
                );
                view.setPadding(
                        view.getPaddingLeft(),
                        originPaddingTop + actionBarHeight + systemInsets.top,
                        view.getPaddingRight(),
                        view.getPaddingBottom()
                );
                return windowInsets;
            });

            ViewCompat.requestApplyInsets(view);
        }
    }

    private void setRootBottomInset(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            int originalPaddingLeft = view.getPaddingLeft();
            int originalPaddingRight = view.getPaddingRight();
            int originalPaddingBottom = view.getPaddingBottom();

            ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
                Insets navigationInsets =
                        windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars());
                int imeBottom = windowInsets.isVisible(WindowInsetsCompat.Type.ime())
                        ? windowInsets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                        : 0;
                int bottomPadding = Math.max(navigationInsets.bottom, imeBottom);

                view.setPadding(
                        originalPaddingLeft + navigationInsets.left,
                        view.getPaddingTop(),
                        originalPaddingRight + navigationInsets.right,
                        originalPaddingBottom + bottomPadding
                );
                return windowInsets;
            });

            ViewCompat.requestApplyInsets(view);
        }
    }

    private int resolveActionBarHeight() {
        TypedValue typedValue = new TypedValue();
        if (getTheme().resolveAttribute(androidx.appcompat.R.attr.actionBarSize, typedValue, true)) {
            return TypedValue.complexToDimensionPixelSize(typedValue.data, getResources().getDisplayMetrics());
        }
        return 0;
    }

    private void applySystemBarStyle() {
        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        WindowInsetsControllerCompat controller =
                new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(true);
        controller.setAppearanceLightNavigationBars(true);
    }

    private void setActionBarTopInset() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            View actionBarContainer = findViewById(androidx.appcompat.R.id.action_bar_container);
            if (actionBarContainer == null) {
                return;
            }
            int originalPaddingTop = actionBarContainer.getPaddingTop();

            ViewCompat.setOnApplyWindowInsetsListener(actionBarContainer, (v, windowInsets) -> {
                int topInsets = windowInsets.getInsets(
                        WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.displayCutout()
                ).top;
                actionBarContainer.setPadding(
                        actionBarContainer.getPaddingLeft(),
                        originalPaddingTop + topInsets,
                        actionBarContainer.getPaddingRight(),
                        actionBarContainer.getPaddingBottom()
                );
                return windowInsets;
            });

            ViewCompat.requestApplyInsets(actionBarContainer);
        }
    }
}
