/*
 * Copyright (c) Microsoft Corporation.
 * Licensed under the MIT License
 */
package com.microsoft.crossdevicesdk.continuity

internal enum class AppContextRequestStatus {
    VALID,
    INVALID_URI_PROVIDER,
    INVALID_REQUESTER_PACKAGE,
    SIGNATURE_CHECK_FAILED,
}
