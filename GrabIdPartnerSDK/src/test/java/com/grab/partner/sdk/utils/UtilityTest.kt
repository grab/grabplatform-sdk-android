/*
 * Copyright (c) Grab Taxi Holdings PTE LTD (GRAB)
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 */
package com.grab.partner.sdk.utils

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.google.gson.Gson
import com.grab.partner.sdk.models.ProtocolInfo
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test

class UtilityTest {
    private var packageManager = mock<PackageManager>()
    private var utility = Utility()
    private val testPackage1 = "com.test1"
    private val minVersionTestPackage1 = "5.54.0"
    private val testPackage2 = "com.test2"
    private val minVersionTestPackage2 = "5.55.0"
    private val packageProtocol = "grab://open"
    private lateinit var protocol1: ProtocolInfo
    private lateinit var protocol2: ProtocolInfo

    @Test
    fun `test isPackageInstalled with no protocol list`() {
        var observer = utility.isPackageInstalled(listOf(), packageManager).test()
        observer.assertComplete()
        observer.assertResult()
    }

    @Test
    fun `test isPackageInstalled with valid package info from first protocol from the protocol list`() {
        whenever(packageManager.getPackageInfo(testPackage1, 0)).thenReturn(createPackageInfo(testPackage1, "5.56.0"))
        var observer = utility.isPackageInstalled(createProtocolList(), packageManager).test()
        observer.assertComplete()
        observer.assertResult(protocol1)
    }

    @Test
    fun `test isPackageInstalled with valid package info from second protocol from the protocol list`() {
        whenever(packageManager.getPackageInfo(testPackage2, 0)).thenReturn(createPackageInfo(testPackage2, "5.56.0"))
        var observer = utility.isPackageInstalled(createProtocolList(), packageManager).test()
        observer.assertComplete()
        observer.assertResult(protocol2)
    }

    @Test
    fun `test isPackageInstalled when installed app version is less than required`() {
        whenever(packageManager.getPackageInfo(testPackage1, 0)).thenReturn(createPackageInfo(testPackage1, "5.53.0"))
        var observer = utility.isPackageInstalled(createProtocolList(), packageManager).test()
        observer.assertComplete()
        observer.assertResult()
    }

    @Test
    fun `test isPackageInstalled when installed app version has invalid digits`() {
        whenever(packageManager.getPackageInfo(testPackage1, 0)).thenReturn(createPackageInfo(testPackage1, "5.53.a"))
        var observer = utility.isPackageInstalled(createProtocolList(), packageManager).test()
        observer.assertComplete()
        observer.assertResult()
    }

    private fun createProtocolList(): List<String> {
        var gson = Gson()
        protocol1 = ProtocolInfo(testPackage1, minVersionTestPackage1, packageProtocol)
        protocol2 = ProtocolInfo(testPackage2, minVersionTestPackage2, packageProtocol)
        return listOf(gson.toJson(protocol1), gson.toJson(protocol2))
    }

    private fun createPackageInfo(packageName: String, versionName: String): PackageInfo {
        var packageInfo = PackageInfo()
        packageInfo.packageName = packageName
        packageInfo.versionName = versionName
        return packageInfo
    }
}