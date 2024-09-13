# GrabID Partner SDK - Android

The GrabId Partner SDK allows users to sign in with their Grab account from third-party apps.

Please visit [our developer site](https://developers.grab.com) for integration instructions, documentation, support information,
and terms of service.

## Example

To run the example project, clone the repo, and run the app in the emulator and walk through the login flow in sequence.

## Requirements
- API level: minimum API level supported by SDK is 21
- Android Build Tool 33.0.1
- Android 34 support


## Installation GrabID Partner SDK

Add dependency in your `build.gradle` file as `implementation 'com.grab.grabidpartnersdk:GrabIdPartnerSDK:x.x.x'`. 

For versions 2.0.9+, GrabID Partner SDK is available in MavenCentral repository.
For versions 1.1 - 2.0.6, GrabID Partner SDK is available in JCenter repository.

## Getting Started

### SDK Configuration
First, you need to register an application to Grab and get credentials for your app from GrabID team.

Then, update the AndroidManifest.xml file your project with required information.
Add the following snippet, replacing the placeholders within the square brackets (`[]`):

### Setup mandatory parameters in AndroidManifest.xml file as below
```
<meta-dataandroid:name="com.grab.partner.sdk.ClientId" android:value="[obtain from GrabId team]" />
<meta-dataandroid:name="com.grab.partner.sdk.RedirectURI" android:value="[Redirect Url must register with Grab Id team during Partner Application registration process]" />
<meta-dataandroid:name="com.grab.partner.sdk.Scope" android:value="[obtain from GrabId team]" />
<meta-dataandroid:name="com.grab.partner.sdk.AcrValues" android:value="[optional parameter: sample value: consent_ctx:countryCode=SG service:PASSENGER, if presence this will add query string parameter &acr_values=<acr value string> in the /authorize api call]" />
<meta-dataandroid:name="com.grab.partner.sdk.Request" android:value="[optional parameter: if presence this will add query string parameter &request=<request string> in the /authorize api call]" />
<meta-dataandroid:name="com.grab.partner.sdk.LoginHint" android:value="[optional parameter: if presence this will add query string parameter &id_token_hint=<login hint string> in the /authorize api call]" />
<meta-dataandroid:name="com.grab.partner.sdk.ServiceDiscoveryUrl" android:value="[obtain from GrabId team]" />
```

## Access the GrabId Partner SDK APIs

Initialize GrabIdPartner from the entry point of Partner application, usually inside on the `onCreate` of MainApplication class as below
```
GrabIdPartner.instance.initialize(applicationContext, null)
```

### Login with GrabId service

Use the `loadLoginSession` API to create the LoginSession object using configurations from the `AndroidManifest.xml`. There is another `loadLoginSession` API that can read all the configurations parameters as function parameters, if we use this contract then no need to set the parameters inside `AndroidManifest.xml`. Use the LoginSession object received from `loadLoginSession` API callback, to call the `loginV2` api and complete the login flow, `loginV2` expects an Activity context.
```
private var loginSession: LoginSession? = null
    fun clickLogin() {
        progressBarVisibility.set(View.VISIBLE)
        stringMessage.set("")
        GrabIdPartner.instance.loadLoginSession(object : LoginSessionCallback {
            override fun onSuccess(loginSession: LoginSession) {
                MainActivityViewModel.loginSession = loginSession
                GrabIdPartner.instance.loginV2(loginSession, activity, object : LoginCallbackV2 {
                    // callback implementation
                })
            }

            override fun onError(grabIdPartnerError: GrabIdPartnerError) {
            }
        })
    }
  }
```
### Handling URL redirect after user authenticate with Web login

The login API will trigger the Grab web login flow or native app (Grab passenger app) login flow based on the configuration available in the client_public_info_endpoint in the discovery URL. To setup the native app OAuth flow please visit [our developer site](https://developers.grab.com) and configure your clientId accordingly. After the user successfully authorizes the application, Grab Id service will validate the redirect URL in the query parameter. If the redirect URL is registered with Grab Id service, Grab Id service will perform the redirect the browser back to the application with an authorization code, state, and error (if any) in the redirect query parameters.  
Note: If we are meant to login with App but package is unavailable then we will attempt to first launch into playstore link if it has been configured and respond via onError for callback with either:
failedTolaunchAppStoreLink,     // Failed to launch the configured app store link
launchAppStoreLink // Launch the configured app store link

Please visit [our developer site](https://developers.grab.com) for integration instructions, documentation, support information,


Partner app activity which will listen to the deep link redirect from the Grab ID service, have to set the appropriate `intent-filter` inside partner app `AndroidManifest.xml` file as below
```
<?xml version="1.0" encoding="UTF-8"?>
<activity android:name=".views.MainActivity">
  <intent-filter>
    ...
  </intent-filter>
</activity>
```

#### Read the code and state from the redirectUri

Partner application Activity needs to override `onNewIntent` to receive the deep link redirect. After receiving the redirectUrl Partner app can initiate the exchange token flow.
```
override fun onNewIntent(intent: Intent) {
  super.onNewIntent(intent)
  var action = intent.action
  var redirectUrl = intent.dataString
  if (Intent.ACTION_VIEW == action && redirectUrl != null) {
    // initiate the token exchange with GRAB ID Partner SDK
  }
}
```

### Exchange token with GrabID Partner SDK
Once Partner app receive the redirectUrl, then can start the token exchange process using GrabID Partner SDK API `exchangeToken` API.
```
GrabIdPartner.instance.exchangeToken(loginSession, redirectUrl, object : ExchangeTokenCallback {
    // callback implementation
})
```

### Get Id Token Information

Partner application can get information about the id token using the `getIdTokenInfo` API:

```
GrabIdPartner.instance.getIdTokenInfo(loginSession, object : GetIdTokenInfoCallback {
    // callback implementation
})
```

IdToken contains information about the valid time of the token, partner id, partner user id, nonce, issuer, etc

### Check if token is valid

#### Access Token

Application can use the isValidAccessToken API to check if the access token is valid
```
let isValid = grabIdPartner.isValidAccessToken(loginSession: loginSession)
```
#### Id Token

Application can use the isValidIdToken API to check if the access token is valid
```
boolean isValid = grabIdPartner.isValidIdToken(idTokenInfo: IdTokenInfo)
```

### Logout

Partner application can logout using the logout API. Currently logout removes cached LoginSession and IdTokenInfo from the Android KeyStore and SharedPreferences. It does not support revoking the tokens from Grab Id service. Revoking authorization will be supported in future release.

```
GrabIdPartner.instance.logout(loginSession, object : LogoutCallback {
    // callback implementation
})
```

### Analytics
GrabID Partner SDK does not send any analytic data due to user privacy. Third-party is responsible for their own analytics to troubleshoot error and analytics data to address their analytics requirements.

## GrabId Partner SDK API

### LoginSession
```
clientId: String              Application registered client id.

redirectUrl: URL              The redirect URL that was used in the initial authorization request.
                              This URL must register with Grab Id service during Partner registration.

scope: String                 Specify the requested permission scopes.

request: String               Partner specific request string.

loginHint: String             Serialized JWT token that client already has, if provided, the user will not be prompted to authenticate

acrValues: [String:String]    Partner specific acr values (name value pairs).



READ ONLY PROPERTIES

code: String?                 Unique authorization code (from query parameter in the redirect url).
                              This code will be used by the exchangeToken API to obtain the accessToken
                              and idToken.

codeVerifier: String?         The code verifier for the PKCE request, that is generated by the login API
                              before the authorization request.

accessTokenExpiresAt: Date?   Access token expiration date.

state: String?                The login API generates a unique state parameter during authorization, the
                              Grab Id service will include the state parameter in the query parameter
                              in the redirect URL to mitigate CSRF attacks.

tokenType: String?            Indicates the grant type of this token request.

nonce: String?                Unique token generated during login.

accessToken: String?          Access token to make Grab API requests on behalf of the user.

idToken: String?              JWT contains user profile information (Signed).

refreshToken: String?         Used to obtain/renewed access token (current not supported).
```

### IdTokenInfo

```
audience: String?             Intended recipient of the token.

service: String?              Service (i.e. PASSENGER).

notValidBefore: Date?         Id token validation start time.

expiration: Date?             Id token expiration date.

issuer: String?               Issuer (i.e. "https://idp.grab.com).

tokenId: String?              Id Token (Unsigned).

partnerId: String?            Partner Id.

partnerUserId: String?        Partner User id

nonce: String?                Unique token generated during login. The value should match the
                              nonce in the LoginSession.
```

### GrabIdPartnerProtocol
```
loadLoginSession(callback: (LoginSession?, GrabIdPartnerError?) -> Unit)

Read the ClientId, Scope, RedirectUrl, Request (optional), AcrValues (optional), IsDebug (optional) from
the application's AndroidManifest file and create a LoginSession.

Completion Handler
Receives a LoginSession with null error if the information in the AndroidManifest is valid, Otherwise the
completion handler will be called with null LoginSession and a GrabIdPartnerError.
```

```
login(loginSession: LoginSession, context: Context, callback: (GrabIdPartnerError?) -> Unit)

The login API will trigger the Grab login flow. If the partner app is configured in client_public_info_endpoint (contained in the discovery URL) to take advantage of native app login state (i.e. PAX, DAX, MEX, etc.). The login API will proxy the authorization request to the native app instead of using default Grab web login flow. Partner can also configure to go to the app store instead of the Grab web login flow. In this case, partner will need to handle error for retry or cancel the Grab login request. To setup the native app OAuth flow please visit our developer site and configure your clientId accordingly. During OAuth flow after the user successfully authorizes the application, Grab Id service will validate the redirect URL in the query parameter with the registered redirect URL, if matches Grab Id service will perform the redirect back to the caller application with an authorization code, state, and error (if any) in the redirect query parameters.
SDK will give native app as first priority to complete the OAuth flow if native app is configured for the clientId. If native app is not installed in the device and clientId has an appstore_link, then SDK will launch the corresponding app in the Google Play Store for user to install, and will notify the caller application by invoking the callback at onError with either of the following:
    a. failedTolaunchAppStoreLink,     // Failed to launch the configured app store link
    b. launchAppStoreLink,             // Launch the configured app store link
Note: If any client app adds appstore_link configuration but user doesn't install the required native app, the OAuth flow will not be completed.
For clients where native app is not installed and no appstore_link is configured then SDK will launch normal web flow to complete the OAuth.

Completion Handler
Null if no error, GrabIdPartnerError with error code and message otherwise.
```

```
exchangeToken(loginSession: LoginSession, redirectUrl: String, callback: (GrabIdPartnerError?) -> Unit)

The exchangeToken API is called after Partner app receive the redirect url via deep link after the in-app web authorization. Application should forward the redirect url to the exchangeToken API. The exchangeToken API will check for redirect error, validate the state in the query parameter against the state in the loginSession.  It will send the authorization code to Grab Id token service to get access token, id token, and access token expiration information. The entire API response will be encrypted and will be saved in the.

Completion Handler
Null if no error, GrabIdPartnerError with error code and message otherwise.

```

```
loginCompleted(loginSession: LoginSession) : Boolean

Not implemented yet

```

```
logout(loginSession: LoginSession, callback: (GrabIdPartnerError?) -> Unit)

The logout API will remove cached loginSession and related idTokenInfo.  Calling login after logout or with an expired access token will trigger the in-app web authorization flow. Revoke token support will be added in future release.

Completion Handler
Null if no error, GrabIdPartnerError with error code and message otherwise.

```

```
getIdTokenInfo(loginSession: LoginSession, callback: (IdTokenInfo?, GrabIdPartnerError?) -> Unit)

Application can call the getIdTokenInfo API to get the idToken information (i.e. id token expiration date, unsigned Id token, nonce,  audience , partner user id, partner id, etc.). It caches the id token in the SharedPreferences as encrypted text using securedRSA key that will be stored in Android KeyStore. . getIdTokenInfo will return IdTokenInfo from cache if the Id token has not expired. getIdTokenInfo API will return error if the Id Token has expired. Application must call the login API to get a new Id Token before calling getIdTokenInfo API to get the IdTokenInfo/

Completion Handler
If there is no error, idTokenInfo contains id token information, otherwise idTokenInfo will be null
If there is error, idTokenInfo will be null and GrabIdPartnerError will contain the error code and message.
```

```
isValidAccessToken(loginSession: LoginSession): Boolean

Determine if the access token in the LoginSession is valid (using the access token expiration date).

Return
true if the access token is valid, false otherwise
```

```
isValidIdToken(idTokenInfo: IdTokenInfo): Boolean

Determine id token in IdTokenInfo is valid (using the id token start time and expiration date).

Return
true if the id token is valid, false otherwise
```

### Grab Id SDK Error

#### GrabIdPartnerError
```
grabIdPartnerErrorDomain: GrabIdPartnerErrorDomain    Error domain
code : GrabIdPartnerErrorCode                         Error code
localizeMessage : String?                             Error message
serviceError : Throwable?                             System Error
```
##### GrabIdPartnerErrorCode
```
unknown,                        // This error is unexpected or cannot be exposed.
network,                        // There is an issue with network connectivity.
sdkNotInitialized,              // SDK is not initialized
invalidClientId,                // Invalid client id
errorInLoginSessionObject,      // Error in loginSession object
errorRetrievingObjectFromSharedPref, // Error retrieving object from shared preferences
invalidRedirectURI,             // Invalid redirect uri
invalidDiscoveryEndpoint,       // Invalid discovery endpoint
errorInDiscoveryEndpoint,       // Error while fetching discovery endpoint
errorInExchangeTokenEndpoint,   // Error while fetching discovery endpoint
missingAccessToken,             // Access token is missing
missingIdToken,                 // Id token is missing
missingAccessTokenExpiry,       // Access token expiry is missing
errorInGetIdTokenInfo,          // Error in getIdTokenInfo endpoint
invalidAuthorizationCode,       // Invalid authorization code
invalidAuthorizationUrl,        // The authorize end point is invalid
invalidPartnerId,               // Partner id is not set in AndroidManifest.
invalidPartnerScope,            // Partner scope is not set in AndroidManifest
unAuthorized,                   // Partner application is unauthorized.
serviceUnavailable,             // The service was not available.
internalServerError,            // There was an issue with Grab server.
invalidAccessToken,             // The access token is invalid.
invalidIdToken,                 // The id token is invalid.
invalidCode,                    // The code is invalid
stateMismatch,                  // State received from the redirect url doesn't match with loginSession
invalidNonce,                   // The nonce is invalid
invalidResponse,                // Unexpected response from GrabId service
errorInLogout                   // Error in logout operation
failedTolaunchAppStoreLink,     // Failed to launch the configured app store link
launchAppStoreLink,             // Launch the configured app store link
loginCancelledByUser,           // user cancelled login flow by closing the Chrome tab
```

## License

MIT License
