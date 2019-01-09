# GrabID Partner SDK - Android
[ ![Download](https://api.bintray.com/packages/grab/GrabIdPartnerSDK/GrabIdPartnerSDK/images/download.svg) ](https://bintray.com/grab/GrabIdPartnerSDK/GrabIdPartnerSDK/_latestVersion)

The GrabId Partner SDK allows users to sign in with their Grab account from third-party apps.

Please visit [our developer site](https://developers.grab.com) for integration instructions, documentation, support information,
and terms of service.

## Example

To run the example project, clone the repo, and run the app in the emulator and walk through the login flow in sequence.

## Requirements
- API level: minimum API level supported by SDK is 19
- Android Build Tool 28.0.3


## Installation GrabID Partner SDK

Add dependency in your `build.gradle` file as `implementation 'com.grab.grabidpartnersdk:GrabIdPartnerSDK:x.x.x'`. GrabID Partner SDK is available in JCenter repository.

## Getting Started

### SDK Configuration
Firstly, you need to register an application to Grab and get credentials for your app from GrabID team.

Then, update the AndroidManifest.xml file your project with required information.
Add the following snippet, replacing the placeholders within the square brackets (`[]`):

### Setup mandatory parameters in AndroidManifest.xml file as below
```
<meta-dataandroid:name="com.grab.partner.sdk.ClientId" android:value="[obtain from GrabId team]" />
<meta-dataandroid:name="com.grab.partner.sdk.RedirectURI" android:value="[Redirect Url must register with Grab Id team during Partner Application registration process]" />
<meta-dataandroid:name="com.grab.partner.sdk.Scope" android:value="[obtain from GrabId team]" />
<meta-dataandroid:name="com.grab.partner.sdk.AcrValues" android:value="[optional parameter: sample value: consent_ctx:countryCode=SG service:PASSENGER, if presence this will add query string parameter &acr_values=<acr value string> in the /authorize api call]" />
<meta-dataandroid:name="com.grab.partner.sdk.Request" android:value="[optional parameter: if presence this will add query string parameter &request=<request string> in the /authorize api call]" />
<meta-dataandroid:name="com.grab.partner.sdk.LoginHint" android:value="[optional parameter: if presence this will add query string parameter &login_hint=<login hint string> in the /authorize api call]" />
<meta-dataandroid:name="com.grab.partner.sdk.ServiceDiscoveryUrl" android:value="[obtain from GrabId team]" />
```

## Access the GrabId Partner SDK APIs

Initialize GrabIdPartner from the entry point of Partner application, usually inside on the `onCreate` of MainApplication class as below
```
GrabIdPartner.instance.initialize(applicationContext)
```

### Login with GrabId service

Using the `loadLoginSession` API to create the LoginSession object using configurations from the `AndroidManifest.xml`. Use this LoginSession to call the `login` api to complete the login flow.
```
private var loginSession: LoginSession? = null
fun clickLogin() {
  GrabIdPartner.instance.loadLoginSession(this::loadLoginSessionCallBack)
}

/**
* callback for loadLoginSession api
*/
private fun loadLoginSessionCallBack(loginSession: LoginSession?, grabIdPartnerError: GrabIdPartnerError?) {
  if (loginSession != null) {
    this.loginSession = loginSession
    GrabIdPartner.instance.login(loginSession, context, this::loginCallBack)
    } else {
      if (grabIdPartnerError != null) {
       // received the GrabIdPartnerError
      }
    }
  }
```
### Handling URL redirect after user authenticate with Web login

The login API will trigger the Grab web login flow. After the user successfully authorizes the application, Grab Id service will validate the redirect URL in the query parameter. If the redirect URL is registered with Grab Id service, Grab Id service will perform the redirect the browser back to the application with an authorization code, state, and error (if any) in the redirect query parameters.  

#### Activity launchMode
Partner app activity which will listen to the deep link redirect from the Grab ID service, have to set the launchMode to `singleTask` or `singleTop` inside partner app `AndroidManifest.xml` file as below
```
<?xml version="1.0" encoding="UTF-8"?>
<activity            
android:name=".views.MainActivity"            
android:launchMode="singleTask">
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
Once Partner app receive the redirectUrl, then can start the token exchange process using GrabID Partner SDK API `exchangeToken`.
```
GrabIdPartner.instance.exchangeToken(loginSession!!, redirectUrl, this::exchangeTokenApiCallBack)
```

### Get Id Token Information

Partner application can get information about the id token using the getIdTokenInfo API:

```
GrabIdPartner.instance.getIdTokenInfo(loginSession!!, this::getIdTokenInfoCallBack)
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
GrabIdPartner.instance.logout(loginSession, this::clearGrabSignInSessionCallback)
```

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

The login API validates the access token in the loginSession. If the access token has not expired. It will setup the LoginSession with cached LoginSession and call the completion handler without error. Otherwise, it gets the GrabId service end points from Grab Id discovery service and generates an unique code verifier, nonce, state as security attributes. The security attributes, redirect URL, request string, and acr values are included in the query parameters to create the Grab Id authorize URL.

Then the login API will launch the Chrome Custom Tabs and navigate to Grab Id authorize URL for in-app web authorization. Once the user finished logging in. Grab Id authorize service will redirect back to the app with the redirect url that includes the authorization code, state, and error (if any). Application must handle the URL redirect as described above.

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
```

## License

MIT License
