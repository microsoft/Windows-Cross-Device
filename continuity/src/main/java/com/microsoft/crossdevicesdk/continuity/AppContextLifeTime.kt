/*
 * Copyright (c) Microsoft Corporation.
 * Licensed under the MIT License
 */
package com.microsoft.crossdevicesdk.continuity

import java.security.InvalidParameterException
import java.util.concurrent.TimeUnit

internal fun normalizeAppContextLifeTime(type: Int, lifeTime: Long?): Long =
    when {
        type != ProtocolConstants.TYPE_RESUME_ACTIVITY ->
            lifeTime ?: TimeUnit.MILLISECONDS.convert(
                ProtocolConstants.APPCONTEXT_DEFAULT_DAYS,
                TimeUnit.DAYS,
            )
        lifeTime == null -> ProtocolConstants.RESUME_ACTIVITY_DEFAULT_LIFE_TIME_MILLIS
        lifeTime <= 0 -> throw InvalidParameterException(
            "${ProtocolConstants.APPCONTEXT_LIFE_TIME_KEY}:" +
                "must be positive when sending resume app context"
        )
        lifeTime <= ProtocolConstants.RESUME_ACTIVITY_MAX_LIFE_TIME_MILLIS -> lifeTime
        else -> ProtocolConstants.RESUME_ACTIVITY_DEFAULT_LIFE_TIME_MILLIS
    }
