/*
 * Copyright (c) Microsoft Corporation.
 * Licensed under the MIT License
 */
package com.microsoft.crossdevicesdk.continuity

import java.util.concurrent.TimeUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class AppContextLifeTimeTest {
    @Test
    fun normalizeAppContextLifeTimeCapsResumeActivityAtFiveMinutes() {
        val maximumLifeTime = ProtocolConstants.RESUME_ACTIVITY_MAX_LIFE_TIME_MILLIS
        val defaultLifeTime = ProtocolConstants.RESUME_ACTIVITY_DEFAULT_LIFE_TIME_MILLIS
        val type = ProtocolConstants.TYPE_RESUME_ACTIVITY

        assertEquals(defaultLifeTime, normalizeAppContextLifeTime(type, null))
        assertEquals(defaultLifeTime, normalizeAppContextLifeTime(type, -1))
        assertEquals(defaultLifeTime, normalizeAppContextLifeTime(type, 0))
        assertEquals(1L, normalizeAppContextLifeTime(type, 1))
        assertEquals(maximumLifeTime, normalizeAppContextLifeTime(type, maximumLifeTime))
        assertEquals(defaultLifeTime, normalizeAppContextLifeTime(type, maximumLifeTime + 1))
    }

    @Test
    fun normalizeAppContextLifeTimePreservesBrowserHistoryBehavior() {
        val type = ProtocolConstants.TYPE_BROWSER_HISTORY
        val defaultLifeTime = TimeUnit.MILLISECONDS.convert(
            ProtocolConstants.APPCONTEXT_DEFAULT_DAYS,
            TimeUnit.DAYS,
        )
        val overResumeActivityLimit =
            ProtocolConstants.RESUME_ACTIVITY_MAX_LIFE_TIME_MILLIS + 1

        assertEquals(defaultLifeTime, normalizeAppContextLifeTime(type, null))
        assertEquals(-1L, normalizeAppContextLifeTime(type, -1))
        assertEquals(
            overResumeActivityLimit,
            normalizeAppContextLifeTime(type, overResumeActivityLimit),
        )
    }
}
