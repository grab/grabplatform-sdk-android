/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk.sampleappjava.viewmodel;

import android.content.Context;
import android.text.method.ScrollingMovementMethod;

import com.grab.partner.sdk.ExchangeTokenCallback;
import com.grab.partner.sdk.GetIdTokenInfoCallback;
import com.grab.partner.sdk.GrabIdPartner;
import com.grab.partner.sdk.LoginCallback;
import com.grab.partner.sdk.LoginSessionCallback;
import com.grab.partner.sdk.LogoutCallback;
import com.grab.partner.sdk.models.GrabIdPartnerError;
import com.grab.partner.sdk.models.IdTokenInfo;
import com.grab.partner.sdk.models.LoginSession;
import com.grab.partner.sdk.sampleappjava.databinding.ActivityMainBinding;

import org.jetbrains.annotations.NotNull;

public class MainActivityViewModel {
    GrabIdPartner grabIdPartner = (GrabIdPartner) GrabIdPartner.Companion.getInstance();
    private static LoginSession loginSession = null;
    private Context context = null;
    private String redirectUrl;
    private ActivityMainBinding binding;

    public MainActivityViewModel(Context context, ActivityMainBinding binding) {
        this.context = context;
        this.binding = binding;
    }

    public void startLoginFlow() {
        grabIdPartner.loadLoginSession(new LoginSessionCallback() {
            @Override
            public void onSuccess(@NotNull final LoginSession loginSession) {
                MainActivityViewModel.loginSession = loginSession;
                grabIdPartner.login(loginSession, context, new LoginCallback() {
                    @Override
                    public void onSuccess() {
                        if(!loginSession.getAccessToken().isEmpty()) {
                            printTokenExchangeResponse(MainActivityViewModel.loginSession);
                        }
                    }

                    @Override
                    public void onError(@NotNull GrabIdPartnerError grabIdPartnerError) {
                        printMessage(grabIdPartnerError.getLocalizeMessage());
                    }
                });
            }

            @Override
            public void onError(@NotNull GrabIdPartnerError grabIdPartnerError) {
                printMessage(grabIdPartnerError.getLocalizeMessage());
            }
        });
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public void getToken() {
        if (loginSession != null) {
            grabIdPartner.exchangeToken(loginSession, redirectUrl, new ExchangeTokenCallback() {
                @Override
                public void onSuccess() {
                    printTokenExchangeResponse(loginSession);
                }

                @Override
                public void onError(@NotNull GrabIdPartnerError grabIdPartnerError) {
                    printMessage(grabIdPartnerError.getLocalizeMessage());
                }
            });
        } else {
            printMessage("Please initiate login flow first, loginSession is null");
        }
    }

    public void getIdTokenInfo() {
        if (loginSession != null) {
            grabIdPartner.getIdTokenInfo(loginSession, new GetIdTokenInfoCallback() {

                @Override
                public void onSuccess(@NotNull IdTokenInfo idTokenInfo) {
                    printIdTokenResponse(idTokenInfo);
                }

                @Override
                public void onError(@NotNull GrabIdPartnerError grabIdPartnerError) {
                    String errorMessage = "Error occurred in getIdTokenInfo API. \nError Message: " + grabIdPartnerError.getLocalizeMessage();
                    printMessage(errorMessage);
                }
            });
        } else {
            printMessage("Please initiate login flow first, loginSession is null");
        }
    }

    /**
     * To initiate the logout/clear loginSession process
     */
    public void clearGrabSignInSession() {
        if (loginSession != null) {
            grabIdPartner.logout(loginSession, new LogoutCallback() {
                @Override
                public void onSuccess() {
                    clearTextView();
                    printMessage("Successfully cleared loginSession for the user");
                }

                @Override
                public void onError(@NotNull GrabIdPartnerError grabIdPartnerError) {
                    printMessage(grabIdPartnerError.getLocalizeMessage());
                }
            });
        }
    }

    /**
     * Clear the text view content
     */
    private void clearTextView() {
        binding.defaulttextview.setText("");
    }

    /**
     * Print the token info endpoint response in the TextView control     *
     */
    private void printIdTokenResponse(IdTokenInfo idTokenInfo) {
        binding.defaulttextview.append("\n\n");
        binding.defaulttextview.append("---------------------------------------------------------------------------- \n");
        binding.defaulttextview.append("\nResponse from oauth2/id_tokens/token_info \n");
        binding.defaulttextview.append("audience:\n" + idTokenInfo.getAudience());
        binding.defaulttextview.append("\n\n");
        binding.defaulttextview.append("expiration:\n" + idTokenInfo.getExpiration());
        binding.defaulttextview.append("\n\n");
        binding.defaulttextview.append("issueDate:\n" + idTokenInfo.getIssueDate());
        binding.defaulttextview.append("\n\n");
        binding.defaulttextview.append("issuer:\n" + idTokenInfo.getIssuer());
        binding.defaulttextview.append("\n\n");
        binding.defaulttextview.append("notValidBefore:\n" + idTokenInfo.getNotValidBefore());
        binding.defaulttextview.append("\n\n");
        binding.defaulttextview.append("partnerId:\n" + idTokenInfo.getPartnerId());
        binding.defaulttextview.append("\n\n");
        binding.defaulttextview.append("partnerUserId:\n" + idTokenInfo.getPartnerUserId());
        binding.defaulttextview.append("\n\n");
        binding.defaulttextview.append("service:\n" + idTokenInfo.getService());
        binding.defaulttextview.append("\n\n");
        binding.defaulttextview.append("tokenId:\n" + idTokenInfo.getTokenId());
        binding.defaulttextview.append("\n\n");
        binding.defaulttextview.append("nonce:\n" + idTokenInfo.getNonce());
        // invalidates all binding expressions and requests a new rebind to refresh UI.
        binding.defaulttextview.invalidate();
    }

    /**
     * Print the token exchange endpoint response in the TextView control
     */
    public void printTokenExchangeResponse(LoginSession loginSession) {
        binding.defaulttextview.setMovementMethod(ScrollingMovementMethod.getInstance());
        binding.defaulttextview.setText("---------------------------------------------------------------------------- \n");
        binding.defaulttextview.append("Response from oauth2/token \n");
        binding.defaulttextview.append("access_token:\n" + loginSession.getAccessToken());

        binding.defaulttextview.append("\n\n");
        binding.defaulttextview.append("id_token:\n" + loginSession.getIdToken());

        binding.defaulttextview.append("\n\n");
        binding.defaulttextview.append("refresh_token:\n" + loginSession.getRefreshToken());

        binding.defaulttextview.append("\n\n");
        binding.defaulttextview.append("expires_in:\n" + loginSession.getAccessTokenExpiresAt());

        // invalidates all binding expressions and requests a new rebind to refresh UI.
        binding.defaulttextview.invalidate();
    }

    /**
     * This is to print any message in the default textview
     */
    public void printMessage(String message) {
        binding.defaulttextview.setMovementMethod(ScrollingMovementMethod.getInstance());
        binding.defaulttextview.append("\n\n");
        binding.defaulttextview.append(message);
        binding.defaulttextview.invalidate();
    }
}