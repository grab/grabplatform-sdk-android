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

internal interface GrabIdPartnerRepo {
    fun saveLoginCallback(loginCallback: LoginCallback)
    fun getLoginCallback(): LoginCallback?
    fun saveUri(uri: Uri)
    fun getUri(): Uri?
}

internal class GrabIdPartnerRepoImpl : GrabIdPartnerRepo {
    private var loginCallback: LoginCallback? = null
    private var uri: Uri? = null

    override fun saveLoginCallback(loginCallback: LoginCallback) {
        this.loginCallback = loginCallback
    }

    override fun getLoginCallback(): LoginCallback? {
        return this.loginCallback
    }

    override fun saveUri(uri: Uri) {
        this.uri = uri
    }

    override fun getUri(): Uri? {
        return this.uri
    }
}