/*
 * Copyright (c) Microsoft Corporation.
 * Licensed under the MIT License
 */
package com.microsoft.crossdevicesdk.continuity

internal fun normalizeAppContextLifeTime(lifeTime: Long?): Long =
    lifeTime?.takeIf { it in 1..ProtocolConstants.APPCONTEXT_MAX_LIFE_TIME_MILLIS }
        ?: ProtocolConstants.APPCONTEXT_DEFAULT_LIFE_TIME_MILLIS
