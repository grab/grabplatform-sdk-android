/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk.sampleappkotlin.wrapper.views

import android.graphics.Color
import android.os.Build
import android.util.TypedValue
import androidx.databinding.DataBindingUtil
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.grab.partner.sdk.sampleappkotlin.wrapper.R
import com.grab.partner.sdk.sampleappkotlin.wrapper.databinding.ActivityMainBinding
import com.grab.partner.sdk.sampleappkotlin.wrapper.viewmodel.MainActivityViewModel
import javax.inject.Inject
import com.grab.partner.sdk.sampleappkotlin.wrapper.di.components.DaggerSampleAppComponent
import com.grab.partner.sdk.sampleappkotlin.wrapper.di.modules.SampleAppModule
import kotlin.math.max

class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var mainActivityViewModel: MainActivityViewModel
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.light(Color.WHITE, Color.WHITE),
                navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            )
        }
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setUpDI()
        binding.vm = mainActivityViewModel
        val contentContainer = (binding.root as? ViewGroup)?.getChildAt(0) ?: binding.root
        setContentTopInset(contentContainer)
        setRootBottomInset(binding.root)
        setActionBarTopInset()
    }

    private fun setUpDI() {
        val component = DaggerSampleAppComponent.builder()
                .sampleAppModule(SampleAppModule(this))
                .build()
        component.inject(this)
    }

    private fun setContentTopInset(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            val originPaddingTop = view.paddingTop
            val actionBarHeight = resolveActionBarHeight()

            ViewCompat.setOnApplyWindowInsetsListener(view) { _, windowInsets ->
                val systemInsets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
                )
                view.updatePadding(
                    top = originPaddingTop + actionBarHeight + systemInsets.top,
                )
                windowInsets
            }

            ViewCompat.requestApplyInsets(view)
        }
    }

    private fun setRootBottomInset(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            val originalPaddingLeft = view.paddingLeft
            val originalPaddingRight = view.paddingRight
            val originalPaddingBottom = view.paddingBottom

            ViewCompat.setOnApplyWindowInsetsListener(view) { _, windowInsets ->
                val navigationInsets = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
                val imeBottom = if (windowInsets.isVisible(WindowInsetsCompat.Type.ime())) {
                    windowInsets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                } else {
                    0
                }
                view.updatePadding(
                    left = originalPaddingLeft + navigationInsets.left,
                    right = originalPaddingRight + navigationInsets.right,
                    bottom = originalPaddingBottom + max(navigationInsets.bottom, imeBottom)
                )
                windowInsets
            }

            ViewCompat.requestApplyInsets(view)
        }
    }

    private fun resolveActionBarHeight(): Int {
        val typedValue = TypedValue()
        return if (theme.resolveAttribute(androidx.appcompat.R.attr.actionBarSize, typedValue, true)) {
            TypedValue.complexToDimensionPixelSize(typedValue.data, resources.displayMetrics)
        } else {
            0
        }
    }

    private fun setActionBarTopInset() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            val actionBarContainer = findViewById<View?>(androidx.appcompat.R.id.action_bar_container) ?: return
            val originalPaddingTop = actionBarContainer.paddingTop

            ViewCompat.setOnApplyWindowInsetsListener(actionBarContainer) { _, windowInsets ->
                val topInsets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.displayCutout()
                ).top
                actionBarContainer.updatePadding(top = originalPaddingTop + topInsets)
                windowInsets
            }

            ViewCompat.requestApplyInsets(actionBarContainer)
        }
    }
}