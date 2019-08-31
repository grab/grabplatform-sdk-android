/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */
package com.grab.partner.sdk.utils

import org.junit.Assert
import org.junit.Test

class VersionTest {
    @Test
    fun `test compareTo with first version is greater than second version`() {
        Assert.assertEquals(1, Version("5.53.0").compareTo(Version("5.52.0")))
        Assert.assertEquals(1, Version("5.53.0").compareTo(Version("4.52.0")))
        Assert.assertEquals(1, Version("5.53.01").compareTo(Version("4.53.0")))
        Assert.assertEquals(1, Version("5.53.0.1").compareTo(Version("5.53.0.0")))
    }

    @Test
    fun `test compareTo with first version is less than second version`() {
        Assert.assertEquals(-1, Version("5.51.0").compareTo(Version("5.52.0")))

    }

    @Test
    fun `test compareTo with first version is equal to the second version`() {
        Assert.assertEquals(0, Version("5.53.0").compareTo(Version("5.53.0")))
    }

    @Test
    fun `test compareTo with first version length is not equal to the second version`() {
        Assert.assertEquals(0, Version("5.000.000.0").compareTo(Version("5")))
        Assert.assertEquals(0, Version("5.001").compareTo(Version("5.1")))
    }

    @Test
    fun `test compareTo one of the version has invalid digits`() {
        Assert.assertEquals(0, Version("5.0a").compareTo(Version("5")))
    }

    @Test
    fun `test compareTo with single digits versions`() {
        Assert.assertEquals(1, Version("2").compareTo(Version("1")))
    }
}