/*
 * Copyright (c) Microsoft Corporation.
 * Licensed under the MIT License
 */
package com.microsoft.crossdevicesdk.continuity

import java.util.concurrent.TimeUnit

internal fun normalizeAppContextLifeTime(type: Int, lifeTime: Long?): Long =
    if (type == ProtocolConstants.TYPE_RESUME_ACTIVITY) {
        lifeTime?.takeIf { it in 1..ProtocolConstants.RESUME_ACTIVITY_MAX_LIFE_TIME_MILLIS }
            ?: ProtocolConstants.RESUME_ACTIVITY_DEFAULT_LIFE_TIME_MILLIS
    } else {
        lifeTime ?: TimeUnit.MILLISECONDS.convert(
            ProtocolConstants.APPCONTEXT_DEFAULT_DAYS,
            TimeUnit.DAYS,
        )
    }
