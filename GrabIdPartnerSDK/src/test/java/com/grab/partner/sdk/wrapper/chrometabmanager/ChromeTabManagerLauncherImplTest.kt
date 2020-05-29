package com.grab.partner.sdk.wrapper.chrometabmanager

import android.content.Context
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
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