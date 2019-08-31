/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */

package com.grab.partner.sdk.utils

import java.util.*

/**
 * Version class to compare two version number strings
 */
class Version(version: String) : Comparable<Version> {
    private val version: String = version

    override fun compareTo(other: Version): Int {
        Scanner(this.version).use { s1 ->
            Scanner(other.version).use { s2 ->
                s1.useDelimiter("\\.")
                s2.useDelimiter("\\.")

                while (s1.hasNextInt() && s2.hasNextInt()) {
                    val v1 = s1.nextInt()
                    val v2 = s2.nextInt()
                    if (v1 < v2) {
                        return -1
                    } else if (v1 > v2) {
                        return 1
                    }
                }

                // if we are here meaning we haven't concluded the decision yet and one of the version string still
                // has some additional digits. Example: 5.02.01 and 5.02
                if (s1.hasNextInt() && s1.nextInt() != 0)
                    return 1 //str1 has an additional lower-level version number
                return if (s2.hasNextInt() && s2.nextInt() != 0) -1 else 0 //str2 has an additional lower-level version
            }
        }
    }
}