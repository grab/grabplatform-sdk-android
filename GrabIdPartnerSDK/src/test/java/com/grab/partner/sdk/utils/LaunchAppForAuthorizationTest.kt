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
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.MockedStatic
import org.mockito.Mockito

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
        testProvider = TestIntentProvider()
        launcher.apply { this.intentProvider = testProvider; }
    }

    @Test
    fun LaunchOAuthFlow_hasPlayLink_shouldLaunchNative() {
        loginSession.deeplinkUriInternal = uri
        loginSession.playstoreLinkInternal = uri_playstore
        val mockStaticUri: MockedStatic<Uri> = Mockito.mockStatic(Uri::class.java)
        try {
            val mockUri: Uri = mock()
            val mockAfterBuildUri: Uri = mock()
            val mockBuilder: Uri.Builder = mock()
            mockStaticUri.`when`<Uri> { Uri.parse(eq(uri)) }.thenReturn(mockUri)
            whenever(mockUri.buildUpon()).thenReturn(mockBuilder)
            whenever(mockBuilder.appendQueryParameter(anyString(), anyString())).thenReturn(mockBuilder)
            whenever(mockBuilder.build()).thenReturn(mockAfterBuildUri)
            whenever(context.packageManager).thenReturn(mock())
            whenever(testProvider.mockIntent.resolveActivity(any())).thenReturn(mock())
            launcher.launchOAuthFlow(context, loginSession, callback, true)
            verify(testProvider.mockImpl).provideIntent(eq(Intent.ACTION_VIEW))
            verify(testProvider.mockIntent).setData(any())
            verify(testProvider.mockIntent).setFlags(any())
            verify(context).startActivity(eq(testProvider.mockIntent))
            verify(callback).onSuccess()
        } finally {
            mockStaticUri.close()
        }
    }

    @Test
    fun LaunchOAuthFlow_hasPlayLink() {
        loginSession.playstoreLinkInternal = uri_playstore
        loginSession.authorizationEndpoint = uri_authEndpoint
        val mockStaticUri: MockedStatic<Uri> = Mockito.mockStatic(Uri::class.java)
        try {
            val mockUri: Uri = mock()
            val mockAfterBuildUri: Uri = mock()
            val mockBuilder: Uri.Builder = mock()
            mockStaticUri.`when`<Uri> { Uri.parse(eq(uri_playstore)) }.thenReturn(mockUri)
            mockStaticUri.`when`<Uri> { Uri.parse(eq(uri_authEndpoint)) }.thenReturn(mockUri)
            whenever(mockUri.buildUpon()).thenReturn(mockBuilder)
            whenever(mockBuilder.appendQueryParameter(anyString(), anyString())).thenReturn(mockBuilder)
            whenever(mockBuilder.build()).thenReturn(mockAfterBuildUri)
            whenever(context.packageManager).thenReturn(mock())
            whenever(testProvider.mockIntent.resolveActivity(any())).thenReturn(mock())
            launcher.launchOAuthFlow(context, loginSession, callback, false)
            verify(testProvider.mockImpl).provideIntent(eq(Intent.ACTION_VIEW))
            verify(testProvider.mockIntent).setData(any())
            verify(testProvider.mockIntent).setFlags(any())
            verify(context).startActivity(eq(testProvider.mockIntent))
            verify(callback).onError(argThat {
                this.grabIdPartnerErrorDomain == GrabIdPartnerErrorDomain.LAUNCHOAUTHFLOW
                        && this.code == GrabIdPartnerErrorCode.launchAppStoreLink
                        && this.localizeMessage == uri_playstore
            })
        } finally {
            mockStaticUri.close()
        }
    }

    @Test
    fun LaunchOAuthFlow_hasPlayLink_fail() {
        context = mock()
        loginSession.playstoreLinkInternal = uri_playstore
        loginSession.authorizationEndpoint = uri_authEndpoint
        val mockStaticUri: MockedStatic<Uri> = Mockito.mockStatic(Uri::class.java)
        try {
            val mockUri: Uri = mock()
            val mockAfterBuildUri: Uri = mock()
            val mockBuilder: Uri.Builder = mock()
            mockStaticUri.`when`<Uri> { Uri.parse(eq(uri_playstore)) }.thenReturn(mockUri)
            mockStaticUri.`when`<Uri> { Uri.parse(eq(uri_authEndpoint)) }.thenReturn(mockUri)
            whenever(mockUri.buildUpon()).thenReturn(mockBuilder)
            whenever(mockBuilder.appendQueryParameter(anyString(), anyString())).thenReturn(mockBuilder)
            whenever(mockBuilder.build()).thenReturn(mockAfterBuildUri)
            whenever(context.packageManager).thenReturn(mock())
            whenever(testProvider.mockIntent.resolveActivity(any())).thenReturn(mock())
            whenever(context.startActivity(any())).thenThrow(NullPointerException())
            launcher.launchOAuthFlow(context, loginSession, callback, false)
            verify(testProvider.mockImpl).provideIntent(eq(Intent.ACTION_VIEW))
            verify(testProvider.mockIntent).setData(any())
            verify(testProvider.mockIntent).setFlags(any())
            verify(context).startActivity(eq(testProvider.mockIntent))
            verify(callback).onError(argThat {
                this.grabIdPartnerErrorDomain == GrabIdPartnerErrorDomain.LAUNCHOAUTHFLOW
                        && this.code == GrabIdPartnerErrorCode.failedTolaunchAppStoreLink
                        && this.localizeMessage == uri_playstore
            })
        } finally {
            mockStaticUri.close()
        }
    }

    @Test
    fun LaunchOAuthFlow_webFlow_shouldLaunchNative() {
        loginSession.deeplinkUriInternal = uri
        loginSession.playstoreLinkInternal = ""
        val mockStaticUri: MockedStatic<Uri> = Mockito.mockStatic(Uri::class.java)
        try {
            val mockUri: Uri = mock()
            val mockAfterBuildUri: Uri = mock()
            val mockBuilder: Uri.Builder = mock()
            mockStaticUri.`when`<Uri> { Uri.parse(eq(uri)) }.thenReturn(mockUri)
            whenever(mockUri.buildUpon()).thenReturn(mockBuilder)
            whenever(mockBuilder.appendQueryParameter(anyString(), anyString())).thenReturn(mockBuilder)
            whenever(mockBuilder.build()).thenReturn(mockAfterBuildUri)
            whenever(context.packageManager).thenReturn(mock())
            whenever(testProvider.mockIntent.resolveActivity(any())).thenReturn(mock())
            launcher.launchOAuthFlow(context, loginSession, callback, true)
            verify(testProvider.mockImpl).provideIntent(eq(Intent.ACTION_VIEW))
            verify(testProvider.mockIntent).setData(any())
            verify(testProvider.mockIntent).setFlags(any())
            verify(context).startActivity(eq(testProvider.mockIntent))
            verify(callback).onSuccess()
        } finally {
            mockStaticUri.close()
        }
    }

    @Test
    fun LaunchOAuthFlow_webFlow() {
        loginSession.deeplinkUriInternal = uri
        loginSession.authorizationEndpoint = uri_authEndpoint
        loginSession.playstoreLinkInternal = ""
        val mockStaticUri: MockedStatic<Uri> = Mockito.mockStatic(Uri::class.java)
        try {
            val mockUri: Uri = mock()
            val mockAfterBuildUri: Uri = mock()
            val mockBuilder: Uri.Builder = mock()
            mockStaticUri.`when`<Uri> { Uri.parse(eq(uri_authEndpoint)) }.thenReturn(mockUri)
            whenever(mockUri.buildUpon()).thenReturn(mockBuilder)
            whenever(mockBuilder.appendQueryParameter(anyString(), anyString())).thenReturn(mockBuilder)
            whenever(mockBuilder.build()).thenReturn(mockAfterBuildUri)
            whenever(context.packageManager).thenReturn(mock())
            whenever(testProvider.mockIntent.resolveActivity(any())).thenReturn(mock())
            launcher.launchOAuthFlow(context, loginSession, callback, false)
            verify(chromeTabLauncher).launchChromeTab(context, mockAfterBuildUri, callback)
        } finally {
            mockStaticUri.close()
        }
    }

    @Test
    fun LaunchOAuthFlow_Wrapper_Flow() {
        loginSession.deeplinkUriInternal = uri
        loginSession.authorizationEndpoint = uri_authEndpoint
        loginSession.playstoreLinkInternal = ""
        loginSession.isWrapperFlow = true
        val mockStaticUri: MockedStatic<Uri> = Mockito.mockStatic(Uri::class.java)
        try {
            val mockUri: Uri = mock()
            val mockAfterBuildUri: Uri = mock()
            val mockBuilder: Uri.Builder = mock()
            mockStaticUri.`when`<Uri> { Uri.parse(eq(uri_authEndpoint)) }.thenReturn(mockUri)
            whenever(mockUri.buildUpon()).thenReturn(mockBuilder)
            whenever(mockBuilder.appendQueryParameter(anyString(), anyString())).thenReturn(mockBuilder)
            whenever(mockBuilder.build()).thenReturn(mockAfterBuildUri)
            whenever(context.packageManager).thenReturn(mock())
            whenever(testProvider.mockIntent.resolveActivity(any())).thenReturn(mock())
            launcher.launchOAuthFlow(context, loginSession, callback, false)
            verify(grabIdPartnerRepo).saveLoginCallback(callback)
            verify(grabIdPartnerRepo).saveUri(mockAfterBuildUri)
            verify(chromeManagerActivityLauncher).launchChromeManagerActivity(context)
        } finally {
            mockStaticUri.close()
        }
    }
}
