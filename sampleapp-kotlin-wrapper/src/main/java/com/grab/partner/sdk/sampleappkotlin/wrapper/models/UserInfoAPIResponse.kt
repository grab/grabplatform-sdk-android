/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk.sampleappkotlin.wrapper.models

data class UserInfoAPIResponse(val userID: String, val serviceID: String, val serviceUserID: Long, val authMethod: String)