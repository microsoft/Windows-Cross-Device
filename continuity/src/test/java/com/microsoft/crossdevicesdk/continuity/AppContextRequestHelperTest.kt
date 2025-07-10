/*
 * Copyright (c) Microsoft Corporation.
 * Licensed under the MIT License
 */
package com.microsoft.crossdevicesdk.continuity

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ProviderInfo
import android.content.pm.Signature
import android.content.pm.SigningInfo
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppContextRequestHelperTest {

    private lateinit var mContext: Context

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var packageManager: PackageManager

    @Mock
    private lateinit var provider: ProviderInfo

    @Mock
    private lateinit var packageInfo: PackageInfo

    @Mock
    private lateinit var signingInfo: SigningInfo

    private var resources: android.content.res.Resources? = null

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        mContext = ApplicationProvider.getApplicationContext()
        resources = mContext.resources
        Mockito.`when`(context.packageManager).thenReturn(packageManager)
        Mockito.`when`(context.resources).thenReturn(resources)
    }

    @After
    fun tearDown() {
        // No-op
    }

    @Test
    fun validateContentProviderAuthority() {
        var uri = Uri.parse("content://com.microsoft.crossdevice")
        var result = AppContextRequestHelper.validateContentProviderAuthority(context, uri)
        assertEquals(AppContextRequestStatus.INVALID_URI_PROVIDER, result)

        uri = Uri.parse("content://com.microsoft.crossdevice/b695d1d8")
        Mockito.doReturn(provider).`when`(packageManager)
            .resolveContentProvider(uri.authority!!, 0)
        provider.packageName = "com.microsoft.appmanagerfake"
        result = AppContextRequestHelper.validateContentProviderAuthority(context, uri)
        assertEquals(AppContextRequestStatus.INVALID_REQUESTER_PACKAGE, result)

        provider.packageName = "com.microsoft.appmanager"
        Mockito.`when`(
            packageManager.getPackageInfo(
                provider.packageName,
                PackageManager.GET_SIGNING_CERTIFICATES,
            ),
        ).thenReturn(packageInfo)
        packageInfo.signingInfo = signingInfo

        // Test when signatures are null
        Mockito.`when`(signingInfo.signingCertificateHistory).thenReturn(null)
        result = AppContextRequestHelper.validateContentProviderAuthority(context, uri)
        assertEquals(AppContextRequestStatus.SIGNATURE_CHECK_FAILED, result)

        val signatures = arrayOf(Signature(""))
        Mockito.`when`(signingInfo.signingCertificateHistory).thenReturn(signatures)
        result = AppContextRequestHelper.validateContentProviderAuthority(context, uri)
        assertEquals(AppContextRequestStatus.VALID, result)
    }
}
