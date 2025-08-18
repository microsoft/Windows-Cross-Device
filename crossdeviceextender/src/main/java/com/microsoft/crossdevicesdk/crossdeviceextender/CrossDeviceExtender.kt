/*
 * Copyright (c) Microsoft Corporation.
 * Licensed under the MIT License
 */
package com.microsoft.crossdevicesdk.crossdeviceextender
import android.app.Notification
import android.os.Build
import android.os.Bundle

/**
 * Notification Extender that sets Serializable payload on notification with Publisher information
 *
 */
class CrossDeviceExtender : Notification.Extender {

    companion object {
        private const val EXTRA_CROSS_DEVICE_EXTENDER: String =
            "com.microsoft.crossdevicesdk.EXTENSIONS"

        /**
         * Gets CrossDeviceNotification object with Publisher Information
         * @param notification Notification from App
         */
        fun getCrossDeviceNotification(notification: Notification): CrossDeviceNotification? {
            val metadata = notification.extras
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                metadata?.getParcelable(
                    EXTRA_CROSS_DEVICE_EXTENDER,
                    CrossDeviceNotification::class.java
                )
            } else {
                metadata?.getParcelable(EXTRA_CROSS_DEVICE_EXTENDER) as? CrossDeviceNotification
            }
        }
    }

    private var crossDeviceNotification: CrossDeviceNotification? = null

    /**
     * Sets the CrossDeviceNotification Publisher information object
     * @param data CrossDeviceNotification object
     * @return Notification.Builder object
     */
    fun setCrossDeviceNotification(data: CrossDeviceNotification): CrossDeviceExtender {
        crossDeviceNotification = data
        return this
    }

    override fun extend(builder: Notification.Builder): Notification.Builder {
        val metadata = Bundle().apply {
            putParcelable(EXTRA_CROSS_DEVICE_EXTENDER, crossDeviceNotification)
        }
        builder.addExtras(metadata)
        return builder
    }
}
