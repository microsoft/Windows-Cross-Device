/*
 * Copyright (c) Microsoft Corporation.
 * Licensed under the MIT License
 */
package com.microsoft.crossdevicesdk.continuity

import org.junit.Assert.assertEquals
import org.junit.Test

class AppContextLifeTimeTest {
    @Test
    fun normalizeAppContextLifeTimeUsesFiveMinuteDefaultAndCap() {
        val maximumLifeTime = ProtocolConstants.APPCONTEXT_MAX_LIFE_TIME_MILLIS
        val defaultLifeTime = ProtocolConstants.APPCONTEXT_DEFAULT_LIFE_TIME_MILLIS

        assertEquals(defaultLifeTime, normalizeAppContextLifeTime(null))
        assertEquals(defaultLifeTime, normalizeAppContextLifeTime(-1))
        assertEquals(defaultLifeTime, normalizeAppContextLifeTime(0))
        assertEquals(1L, normalizeAppContextLifeTime(1))
        assertEquals(maximumLifeTime, normalizeAppContextLifeTime(maximumLifeTime))
        assertEquals(defaultLifeTime, normalizeAppContextLifeTime(maximumLifeTime + 1))
    }
}
