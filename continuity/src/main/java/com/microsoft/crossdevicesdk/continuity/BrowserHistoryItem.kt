/*
 * Copyright (c) Microsoft Corporation.
 * Licensed under the MIT License
 */
package com.microsoft.crossdevicesdk.continuity

import android.net.Uri
import java.util.Arrays

internal class BrowserHistoryItem(
    val timestamp: Long,
    val webUri: Uri,
    val title: String,
    favIcon: ByteArray?,
) {
    private val favIcon: ByteArray? =
        if (favIcon == null) null else Arrays.copyOf(favIcon, favIcon.size)

    fun getFavIcon(): ByteArray? {
        return favIcon?.clone()
    }
}
