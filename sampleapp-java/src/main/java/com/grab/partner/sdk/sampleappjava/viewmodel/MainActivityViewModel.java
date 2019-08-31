/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk.sampleappjava.viewmodel;

import android.content.Context;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.text.method.ScrollingMovementMethod;

import android.view.View;
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
    private ObservableField<String> stringMessage = new ObservableField<>();
    private ObservableInt progressBarVisibility = new ObservableInt(View.GONE);

    public MainActivityViewModel(Context context, ActivityMainBinding binding) {
        this.context = context;
        this.binding = binding;
        binding.defaulttextview.setMovementMethod(new ScrollingMovementMethod());
    }

    public void startLoginFlow() {
        progressBarVisibility.set(View.VISIBLE);
        grabIdPartner.loadLoginSession(new LoginSessionCallback() {
            @Override
            public void onSuccess(@NotNull final LoginSession loginSession) {
                MainActivityViewModel.loginSession = loginSession;
                grabIdPartner.login(loginSession, context, new LoginCallback() {
                    @Override
                    public void onSuccess() {
                        if (!loginSession.getAccessToken().isEmpty()) {
                            stringMessage.set(createTokenResponse(loginSession));
                        }
                        progressBarVisibility.set(View.GONE);
                    }

                    @Override
                    public void onError(@NotNull GrabIdPartnerError grabIdPartnerError) {
                        stringMessage.set(grabIdPartnerError.getLocalizeMessage());
                        progressBarVisibility.set(View.GONE);
                    }
                });
            }

            @Override
            public void onError(@NotNull GrabIdPartnerError grabIdPartnerError) {
                stringMessage.set(grabIdPartnerError.getLocalizeMessage());
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
                    stringMessage.set(createTokenResponse(loginSession));
                    progressBarVisibility.set(View.GONE);
                }

                @Override
                public void onError(@NotNull GrabIdPartnerError grabIdPartnerError) {
                    stringMessage.set(grabIdPartnerError.getLocalizeMessage());
                    progressBarVisibility.set(View.GONE);
                }
            });
        } else {
            stringMessage.set("Please initiate login flow first, loginSession is null");
            progressBarVisibility.set(View.GONE);
        }
    }

    public void getIdTokenInfo() {
        if (loginSession != null) {
            grabIdPartner.getIdTokenInfo(loginSession, new GetIdTokenInfoCallback() {

                @Override
                public void onSuccess(@NotNull IdTokenInfo idTokenInfo) {
                    stringMessage.set(createIdTokenResponse(idTokenInfo));
                }

                @Override
                public void onError(@NotNull GrabIdPartnerError grabIdPartnerError) {
                    String errorMessage = "Error occurred in getIdTokenInfo API. \nError Message: " + grabIdPartnerError.getLocalizeMessage();
                    stringMessage.set(errorMessage);
                }
            });
        } else {
            stringMessage.set("Please initiate login flow first, loginSession is null");
        }
    }

    public ObservableInt progressBarVisibility() {
        return this.progressBarVisibility;
    }

    public ObservableField<String> stringMessage() {
        return this.stringMessage;
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
                    stringMessage.set("Successfully cleared loginSession for the user");
                }

                @Override
                public void onError(@NotNull GrabIdPartnerError grabIdPartnerError) {
                    stringMessage.set(grabIdPartnerError.getLocalizeMessage());
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

    private String createTokenResponse(LoginSession loginSession) {
        String tokenResponseString = "---------------------------------------------------------------------------- \n" + "Response from oauth2/token" + "\n\n access_token: \n %s" + "\n\n id_token: \n %s" + "\n\n refresh_token: \n %s" + "\n\nexpires_in: \n %s";
        return String.format(tokenResponseString, loginSession.getAccessToken(), loginSession.getIdToken(), loginSession.getRefreshToken(), loginSession.getAccessTokenExpiresAt());
    }

    private String createIdTokenResponse(IdTokenInfo idTokenInfo) {
        String idTokenResponseString = "----------------------------------------------------------------------------\n" + "Response from oauth2/id_tokens/token_info" + "\n\n audience: \n %s" + "\n\n expiration: \n %s" + "\n\n issueDate: \n %s" + "\n\n issuer: \n %s" +
                "\n\n notValidBefore: \n %s" + "\n\n partnerId: \n %s" + "\n\n partnerUserId: \n %s" + "\n\n service: \n %s" + "\n\n tokenId: \n %s" + "\n\n nonce: \n %s";
        return String.format(idTokenResponseString, idTokenInfo.getAudience(), idTokenInfo.getExpiration(), idTokenInfo.getIssueDate(), idTokenInfo.getIssuer(), idTokenInfo.getNotValidBefore(), idTokenInfo.getPartnerId(), idTokenInfo.getPartnerUserId(), idTokenInfo.getService(), idTokenInfo.getTokenId(), idTokenInfo.getNonce());
    }
}