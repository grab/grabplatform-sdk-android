/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk.sampleappjava;

import android.app.Application;
import com.grab.partner.sdk.GrabIdPartner;

public final class MainApplication extends Application {
    public void onCreate() {
        super.onCreate();
        GrabIdPartner grabIdPartner = GrabIdPartner.Companion.getInstance();
        grabIdPartner.initialize(getApplicationContext());
    }
}