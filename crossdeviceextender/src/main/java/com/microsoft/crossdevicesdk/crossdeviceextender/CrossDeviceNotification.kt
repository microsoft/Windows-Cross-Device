/*
 * Copyright (c) Microsoft Corporation.
 * Licensed under the MIT License
 */
package com.microsoft.crossdevicesdk.crossdeviceextender
import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

// Add other fields as we go on extend this for other resume related purposes
/**
 * Data class to retrieve Cross Device notification publisher information across different apps.
 */
@Parcelize
@Keep
class CrossDeviceNotification(val publisherUrl: String) :
    Parcelable {
    val protocolVersion = "1.0"
}
