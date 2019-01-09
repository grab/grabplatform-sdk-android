/*
 * Copyright (c) 2012-2019 Grab Taxi Holdings PTE LTD (GRAB), All Rights Reserved. NOTICE: All information contained herein is, and remains the property of GRAB. The intellectual and technical concepts contained herein are confidential, proprietary and controlled by GRAB and may be covered by patents, patents in process, and are protected by trade secret or copyright law.
 * You are strictly forbidden to copy, download, store (in any medium), transmit, disseminate, adapt or change this material in any way unless prior written permission is obtained from GRAB. Access to the source code contained herein is hereby forbidden to anyone except current GRAB employees or contractors with binding Confidentiality and Non-disclosure agreements explicitly covering such access.
 *
 * The copyright notice above does not evidence any actual or intended publication or disclosure of this source code, which includes information that is confidential and/or proprietary, and is a trade secret, of GRAB.
 * ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC PERFORMANCE, OR PUBLIC DISPLAY OF OR THROUGH USE OF THIS SOURCE CODE WITHOUT THE EXPRESS WRITTEN CONSENT OF GRAB IS STRICTLY PROHIBITED, AND IN VIOLATION OF APPLICABLE LAWS AND INTERNATIONAL TREATIES. THE RECEIPT OR POSSESSION OF THIS SOURCE CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY ANY RIGHTS TO REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR SELL ANYTHING THAT IT MAY DESCRIBE, IN WHOLE OR IN PART.
 */

package com.grab.partner.sdk.sampleapp.views

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.method.ScrollingMovementMethod
import com.grab.partner.sdk.models.IdTokenInfo
import com.grab.partner.sdk.models.LoginSession
import com.grab.partner.sdk.sample.viewmodel.CallBack
import com.grab.partner.sdk.sample.viewmodel.MainActivityViewModel
import com.grab.partner.sdk.sampleapp.MainApplication
import com.grab.partner.sdk.sampleapp.databinding.ActivityMainBinding
import com.grab.partner.sdk.sampleapp.models.UserInfoAPIResponse
import javax.inject.Inject


class MainActivity : AppCompatActivity(), CallBack {
    @Inject
    lateinit var mainActivityViewModel: MainActivityViewModel
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, com.grab.partner.sdk.sampleapp.R.layout.activity_main)
        (application as MainApplication).component.inject(this)
        binding.vm = mainActivityViewModel
        mainActivityViewModel.setCallBack(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        var action = intent.action
        var redirectUrl = intent.dataString
        if (Intent.ACTION_VIEW == action && redirectUrl != null) {
            mainActivityViewModel.setRedirectUrl(redirectUrl)
            // initiate the token exchange with GRAB ID Partner SDK
            mainActivityViewModel.getToken()
        }
    }

    /**
     * Print the token exchange endpoint response in the TextView control
     */
    override fun printTokenExchangeResponse(loginSession: LoginSession) {
        binding.defaulttextview.movementMethod = ScrollingMovementMethod()
        binding.defaulttextview.text = "---------------------------------------------------------------------------- \n"
        binding.defaulttextview.append("Response from oauth2/token \n")
        binding.defaulttextview.append("access_token:\n" + loginSession.accessToken)

        binding.defaulttextview.append("\n\n")
        binding.defaulttextview.append("id_token:\n" + loginSession.idToken)

        binding.defaulttextview.append("\n\n")
        binding.defaulttextview.append("refresh_token:\n" + loginSession.refreshToken)

        binding.defaulttextview.append("\n\n")
        binding.defaulttextview.append("expires_in:\n" + loginSession.accessTokenExpiresAt)

        // invalidates all binding expressions and requests a new rebind to refresh UI.
        binding.defaulttextview.invalidate()
    }

    /**
     * Print the token info endpoint response in the TextView control
     */
    override fun printIdTokenResponse(idTokenInfo: IdTokenInfo) {
        binding.defaulttextview.append("\n\n")
        binding.defaulttextview.append("---------------------------------------------------------------------------- \n")
        binding.defaulttextview.append("\nResponse from oauth2/id_tokens/token_info \n")
        binding.defaulttextview.append("audience:\n" + idTokenInfo.audience)
        binding.defaulttextview.append("\n\n")
        binding.defaulttextview.append("expiration:\n" + idTokenInfo.expiration)
        binding.defaulttextview.append("\n\n")
        binding.defaulttextview.append("issueDate:\n" + idTokenInfo.issueDate)
        binding.defaulttextview.append("\n\n")
        binding.defaulttextview.append("issuer:\n" + idTokenInfo.issuer)
        binding.defaulttextview.append("\n\n")
        binding.defaulttextview.append("notValidBefore:\n" + idTokenInfo.notValidBefore)
        binding.defaulttextview.append("\n\n")
        binding.defaulttextview.append("partnerId:\n" + idTokenInfo.partnerId)
        binding.defaulttextview.append("\n\n")
        binding.defaulttextview.append("partnerUserId:\n" + idTokenInfo.partnerUserId)
        binding.defaulttextview.append("\n\n")
        binding.defaulttextview.append("service:\n" + idTokenInfo.service)
        binding.defaulttextview.append("\n\n")
        binding.defaulttextview.append("tokenId:\n" + idTokenInfo.tokenId)
        binding.defaulttextview.append("\n\n")
        binding.defaulttextview.append("nonce:\n" + idTokenInfo.nonce)
        binding.defaulttextview.invalidate()
    }

    override fun printUserInfoAPIResponse(userInfoAPIResponse: UserInfoAPIResponse) {
        binding.defaulttextview.append("\n\n")
        binding.defaulttextview.append("---------------------------------------------------------------------------- \n")
        binding.defaulttextview.append("\nResponse from oauth2/test_res API \n")
        binding.defaulttextview.append("userID:\n" + userInfoAPIResponse.userID)
        binding.defaulttextview.append("\n\n")
        binding.defaulttextview.append("serviceID:\n" + userInfoAPIResponse.serviceID)
        binding.defaulttextview.append("\n\n")
        binding.defaulttextview.append("serviceUserID:\n" + userInfoAPIResponse.serviceUserID)
        binding.defaulttextview.append("\n\n")
        binding.defaulttextview.append("authMethod:\n" + userInfoAPIResponse.authMethod)
        binding.defaulttextview.invalidate()
    }

    /**
     * Clear the text view content
     */
    override fun clearTextView() {
        binding.defaulttextview.text = ""
    }

    /**
     * This is to print any message in the default textview
     */
    override fun printMessage(message: String) {
        binding.defaulttextview.append("\n\n")
        binding.defaulttextview.append(message)
        binding.defaulttextview.invalidate()
    }
}