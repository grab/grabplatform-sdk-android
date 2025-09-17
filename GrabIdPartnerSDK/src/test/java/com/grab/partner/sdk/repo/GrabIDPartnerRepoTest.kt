/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */
package com.grab.partner.sdk.repo

import android.net.Uri
import com.grab.partner.sdk.LoginCallback
import com.grab.partner.sdk.TestLoginCallback
import org.junit.Assert
import org.junit.Test
import org.mockito.kotlin.mock

class GrabIDPartnerRepoTest {
    private val grabIDPartnerRepo = GrabIdPartnerRepoImpl()

    @Test
    fun testSetAndGetLoginCallback() {
        val loginCallback: LoginCallback = TestLoginCallback()
        grabIDPartnerRepo.saveLoginCallback(loginCallback)
        Assert.assertEquals(loginCallback, grabIDPartnerRepo.getLoginCallback())
    }

    @Test
    fun testSaveAndGetUrl() {
        val testUrl: Uri = mock()
        val testUrl2: Uri = mock()
        grabIDPartnerRepo.saveUri(testUrl)
        Assert.assertEquals(testUrl, grabIDPartnerRepo.getUri())
        Assert.assertNotEquals(testUrl2, grabIDPartnerRepo.getUri())
    }
}