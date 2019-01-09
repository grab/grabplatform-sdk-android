/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk.sampleapp.scheduleprovider

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

open class SchedulerProvider {

    open fun io() : Scheduler = Schedulers.io()

    open fun ui() : Scheduler = AndroidSchedulers.mainThread()
}

class TestSchedulerProvider : SchedulerProvider() {

    override fun io() : Scheduler = Schedulers.trampoline()

    override fun ui() : Scheduler = Schedulers.trampoline()
}
