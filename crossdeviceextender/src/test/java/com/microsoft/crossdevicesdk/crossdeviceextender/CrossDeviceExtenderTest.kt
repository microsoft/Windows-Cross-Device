/*
 * Copyright (c) Microsoft Corporation.
 * Licensed under the MIT License
 */
package com.microsoft.crossdevicesdk.crossdeviceextender
import android.app.Notification
import android.os.Build
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O])
class CrossDeviceExtenderTest {

    @Test
    fun testCrossNotificationExtender() {

        // Arrange
        val context = RuntimeEnvironment.getApplication()
        val notificationMetadata = CrossDeviceNotification("https://publishertestwebsite.com")
        val notificationExtender =
            CrossDeviceExtender().setCrossDeviceNotification(notificationMetadata)

        // Act
        val builder = Notification.Builder(context, "test_channel")
            .setContentTitle("Test Cross Device Notification").extend(notificationExtender)
        val notification = builder.build()
        val retrievedData = CrossDeviceExtender.getCrossDeviceNotification(notification)

        // Assert
        assert(retrievedData != null)
        assert(notificationMetadata.publisherUrl == retrievedData?.publisherUrl)
        assert(notificationMetadata.protocolVersion == retrievedData?.protocolVersion)
    }
}
