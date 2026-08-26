/*
 * Copyright (c) Microsoft Corporation.
 * Licensed under the MIT License
 */
package com.microsoft.crossdevicesdk.continuity

import java.security.InvalidParameterException

internal fun normalizeAppContextLifeTime(type: Int, lifeTime: Long?): Long =
    when {
        type == ProtocolConstants.TYPE_BROWSER_HISTORY ->
            lifeTime ?: ProtocolConstants.BROWSER_HISTORY_DEFAULT_LIFE_TIME_MILLIS
        lifeTime == null -> ProtocolConstants.APPCONTEXT_DEFAULT_LIFE_TIME_MILLIS
        lifeTime <= 0 -> throw InvalidParameterException(
            "${ProtocolConstants.APPCONTEXT_LIFE_TIME_KEY}: " +
                "must be positive when sending app context"
        )
        lifeTime <= ProtocolConstants.APPCONTEXT_MAX_LIFE_TIME_MILLIS -> lifeTime
        else -> ProtocolConstants.APPCONTEXT_MAX_LIFE_TIME_MILLIS
    }

internal fun isAppContextExpired(
    lastUpdatedTime: Long,
    lifeTime: Long,
    currentTime: Long,
): Boolean =
    lifeTime >= 0 &&
        lastUpdatedTime <= Long.MAX_VALUE - lifeTime &&
        lastUpdatedTime + lifeTime < currentTime
