package com.grab.partner.sdk.wrapper.chrometabmanager

import android.content.Context
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.junit.Before
import org.junit.Test

class ChromeTabManagerLauncherImplTest {
    private val context: Context = mock()
    private lateinit var chromeManagerActivityLauncher: ChromeManagerActivityLauncher

    @Before
    fun setUp() {
        chromeManagerActivityLauncher = ChromeTabManagerLauncherImpl()
    }

    @Test
    fun launchChromeManagerActivity() {
        chromeManagerActivityLauncher.launchChromeManagerActivity(context)
        verify(context).startActivity(any())
    }
}