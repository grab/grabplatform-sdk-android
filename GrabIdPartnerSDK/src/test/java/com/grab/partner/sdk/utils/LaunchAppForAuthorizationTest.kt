package com.grab.partner.sdk.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.grab.partner.sdk.LoginCallback
import com.grab.partner.sdk.models.GrabIdPartnerErrorCode
import com.grab.partner.sdk.models.GrabIdPartnerErrorDomain
import com.grab.partner.sdk.models.LoginSession
import com.grab.partner.sdk.repo.GrabIdPartnerRepo
import com.grab.partner.sdk.wrapper.chrometabmanager.ChromeManagerActivityLauncher
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner

@RunWith(PowerMockRunner::class)
@PrepareForTest(Uri::class, Uri.Builder::class)
class LaunchAppForAuthorizationImplTest  {

    private var context: Context = mock()
    private val loginSession = LoginSession().apply {
        this.authorizationEndpoint = "authendpointParm"
        this.clientId = "clientURI"
        this.redirectUri = "redURI"
        this.serviceDiscoveryUrl = "servDiscUrl"
        this.scope = "scope"
        this.request = "request"
        this.loginHint = "loginHint"
        this.idTokenHint = "idTokenHint"
        this.acrValues = "acrVals"
        this.prompt = "prompt"
    }
    private val callback: LoginCallback = mock()
    private var testProvider = TestIntentProvider()
    private val chromeTabLauncher: ChromeTabLauncher = mock()
    private val chromeManagerActivityLauncher: ChromeManagerActivityLauncher = mock()
    private val grabIdPartnerRepo: GrabIdPartnerRepo = mock()
    private val launcher = LaunchAppForAuthorizationImpl(
        chromeTabLauncher,
        chromeManagerActivityLauncher,
        grabIdPartnerRepo
    )
    private val uri = "https://github.com/thing/try"
    private val uri_authEndpoint = "https://github.com/auth/thing"
    private val uri_playstore = "https://playstore.com/thing/try"

    @Before
    fun initialize() {
        PowerMockito.mockStatic(Uri::class.java)
        PowerMockito.mockStatic(Uri.Builder::class.java)
        testProvider = TestIntentProvider()
        launcher.apply { this.intentProvider = testProvider; }
    }

    @Test
    fun LaunchOAuthFlow_hasPlayLink_shouldLaunchNative() {
        loginSession.deeplinkUriInternal = uri
        loginSession.playstoreLinkInternal = uri_playstore
        val mockURI: Uri = PowerMockito.mock(Uri::class.java)
        val afterBuilderURI: Uri = PowerMockito.mock(Uri::class.java)
        val mockBuilder = PowerMockito.mock(Uri.Builder::class.java)
        PowerMockito.`when`(Uri.parse(eq(uri))).thenReturn(mockURI)
        PowerMockito.`when`(mockURI.buildUpon()).thenReturn(mockBuilder)
        PowerMockito.`when`(mockBuilder.appendQueryParameter(anyString(), anyString())).thenReturn(mockBuilder)
        PowerMockito.`when`(mockBuilder.build()).thenReturn(afterBuilderURI)
        whenever(context.packageManager).thenReturn(mock())
        whenever(testProvider.mockIntent.resolveActivity(any())).thenReturn(mock())
        launcher.launchOAuthFlow(context, loginSession, callback, true)
        verify(testProvider.mockImpl).provideIntent(eq(Intent.ACTION_VIEW))
        verify(testProvider.mockIntent).setData(any())
        verify(testProvider.mockIntent).setFlags(any())
        verify(context).startActivity(eq(testProvider.mockIntent))
        verify(callback).onSuccess()
    }

    @Test
    fun LaunchOAuthFlow_hasPlayLink() {
        loginSession.playstoreLinkInternal = uri_playstore
        loginSession.authorizationEndpoint = uri_authEndpoint
        val mockURI: Uri = PowerMockito.mock(Uri::class.java)
        val afterBuilderURI: Uri = PowerMockito.mock(Uri::class.java)
        val mockBuilder = PowerMockito.mock(Uri.Builder::class.java)
        val mockURIForPlayStore: Uri = PowerMockito.mock(Uri::class.java)
        PowerMockito.`when`(Uri.parse(eq(uri_playstore))).thenReturn(mockURIForPlayStore)
        PowerMockito.`when`(Uri.parse(eq(uri_authEndpoint))).thenReturn(mockURI)
        PowerMockito.`when`(mockURI.buildUpon()).thenReturn(mockBuilder)
        PowerMockito.`when`(mockBuilder.appendQueryParameter(anyString(), anyString())).thenReturn(mockBuilder)
        PowerMockito.`when`(mockBuilder.build()).thenReturn(afterBuilderURI)
        whenever(context.packageManager).thenReturn(mock())
        whenever(testProvider.mockIntent.resolveActivity(any())).thenReturn(mock())
        launcher.launchOAuthFlow(context, loginSession, callback, false)
        verify(testProvider.mockImpl).provideIntent(eq(Intent.ACTION_VIEW))
        verify(testProvider.mockIntent).setData(any())
        verify(testProvider.mockIntent).setFlags(any())
        verify(context).startActivity(eq(testProvider.mockIntent))
        verify(callback).onError(argThat{
            this.grabIdPartnerErrorDomain == GrabIdPartnerErrorDomain.LAUNCHOAUTHFLOW
                    && this.code == GrabIdPartnerErrorCode.launchAppStoreLink
                    && this.localizeMessage == uri_playstore
        })
    }

    @Test
    fun LaunchOAuthFlow_hasPlayLink_fail() {
        context = mock()
        loginSession.playstoreLinkInternal = uri_playstore
        loginSession.authorizationEndpoint = uri_authEndpoint
        val mockURI: Uri = PowerMockito.mock(Uri::class.java)
        val afterBuilderURI: Uri = PowerMockito.mock(Uri::class.java)
        val mockBuilder = PowerMockito.mock(Uri.Builder::class.java)
        val mockURIForPlayStore: Uri = PowerMockito.mock(Uri::class.java)
        PowerMockito.`when`(Uri.parse(eq(uri_playstore))).thenReturn(mockURIForPlayStore)
        PowerMockito.`when`(Uri.parse(eq(uri_authEndpoint))).thenReturn(mockURI)
        PowerMockito.`when`(mockURI.buildUpon()).thenReturn(mockBuilder)
        PowerMockito.`when`(mockBuilder.appendQueryParameter(anyString(), anyString())).thenReturn(mockBuilder)
        PowerMockito.`when`(mockBuilder.build()).thenReturn(afterBuilderURI)
        whenever(context.packageManager).thenReturn(mock())
        whenever(testProvider.mockIntent.resolveActivity(any())).thenReturn(mock())
        whenever(context.startActivity(any())).thenThrow(NullPointerException())
        launcher.launchOAuthFlow(context, loginSession, callback, false)
        verify(testProvider.mockImpl).provideIntent(eq(Intent.ACTION_VIEW))
        verify(testProvider.mockIntent).setData(any())
        verify(testProvider.mockIntent).setFlags(any())
        verify(context).startActivity(eq(testProvider.mockIntent))
        verify(callback).onError(argThat{
            this.grabIdPartnerErrorDomain == GrabIdPartnerErrorDomain.LAUNCHOAUTHFLOW
                    && this.code == GrabIdPartnerErrorCode.failedTolaunchAppStoreLink
                    && this.localizeMessage == uri_playstore
        })

    }

    @Test
    fun LaunchOAuthFlow_webFlow_shouldLaunchNative() {
        loginSession.deeplinkUriInternal = uri
        loginSession.playstoreLinkInternal = ""
        val mockURI: Uri = PowerMockito.mock(Uri::class.java)
        val afterBuilderURI: Uri = PowerMockito.mock(Uri::class.java)
        val mockBuilder = PowerMockito.mock(Uri.Builder::class.java)
        PowerMockito.`when`(Uri.parse(eq(uri))).thenReturn(mockURI)
        PowerMockito.`when`(mockURI.buildUpon()).thenReturn(mockBuilder)
        PowerMockito.`when`(mockBuilder.appendQueryParameter(anyString(), anyString())).thenReturn(mockBuilder)
        PowerMockito.`when`(mockBuilder.build()).thenReturn(afterBuilderURI)
        whenever(context.packageManager).thenReturn(mock())
        whenever(testProvider.mockIntent.resolveActivity(any())).thenReturn(mock())
        launcher.launchOAuthFlow(context, loginSession, callback, true)
        verify(testProvider.mockImpl).provideIntent(eq(Intent.ACTION_VIEW))
        verify(testProvider.mockIntent).setData(any())
        verify(testProvider.mockIntent).setFlags(any())
        verify(context).startActivity(eq(testProvider.mockIntent))
        verify(callback).onSuccess()
    }

    @Test
    fun LaunchOAuthFlow_webFlow() {
        loginSession.deeplinkUriInternal = uri
        loginSession.authorizationEndpoint = uri_authEndpoint
        loginSession.playstoreLinkInternal = ""
        val mockURI: Uri = PowerMockito.mock(Uri::class.java)
        val afterBuilderURI: Uri = PowerMockito.mock(Uri::class.java)
        val mockBuilder = PowerMockito.mock(Uri.Builder::class.java)
        PowerMockito.`when`(Uri.parse(eq(uri_authEndpoint))).thenReturn(mockURI)
        PowerMockito.`when`(mockURI.buildUpon()).thenReturn(mockBuilder)
        PowerMockito.`when`(mockBuilder.appendQueryParameter(anyString(), anyString())).thenReturn(mockBuilder)
        PowerMockito.`when`(mockBuilder.build()).thenReturn(afterBuilderURI)
        whenever(context.packageManager).thenReturn(mock())
        whenever(testProvider.mockIntent.resolveActivity(any())).thenReturn(mock())
        launcher.launchOAuthFlow(context, loginSession, callback, false)
        verify(chromeTabLauncher).launchChromeTab(context, afterBuilderURI, callback)
    }

    @Test
    fun LaunchOAuthFlow_Wrapper_Flow() {
        loginSession.deeplinkUriInternal = uri
        loginSession.authorizationEndpoint = uri_authEndpoint
        loginSession.playstoreLinkInternal = ""
        loginSession.isWrapperFlow = true
        val mockURI: Uri = PowerMockito.mock(Uri::class.java)
        val afterBuilderURI: Uri = PowerMockito.mock(Uri::class.java)
        val mockBuilder = PowerMockito.mock(Uri.Builder::class.java)
        PowerMockito.`when`(Uri.parse(eq(uri_authEndpoint))).thenReturn(mockURI)
        PowerMockito.`when`(mockURI.buildUpon()).thenReturn(mockBuilder)
        PowerMockito.`when`(mockBuilder.appendQueryParameter(anyString(), anyString())).thenReturn(mockBuilder)
        PowerMockito.`when`(mockBuilder.build()).thenReturn(afterBuilderURI)
        whenever(context.packageManager).thenReturn(mock())
        whenever(testProvider.mockIntent.resolveActivity(any())).thenReturn(mock())
        launcher.launchOAuthFlow(context, loginSession, callback, false)
        verify(grabIdPartnerRepo).saveLoginCallback(callback)
        verify(grabIdPartnerRepo).saveUri(afterBuilderURI)
        verify(chromeManagerActivityLauncher).launchChromeManagerActivity(context)
    }
}