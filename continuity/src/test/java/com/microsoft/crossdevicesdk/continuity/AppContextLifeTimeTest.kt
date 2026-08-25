/*
 * Copyright (c) Microsoft Corporation.
 * Licensed under the MIT License
 */
package com.microsoft.crossdevicesdk.continuity

import java.security.InvalidParameterException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class AppContextLifeTimeTest {
    @Test
    fun normalizeAppContextLifeTimeUsesFiveMinuteDefaultAndCap() {
        val maximumLifeTime = ProtocolConstants.APPCONTEXT_MAX_LIFE_TIME_MILLIS
        val defaultLifeTime = ProtocolConstants.APPCONTEXT_DEFAULT_LIFE_TIME_MILLIS
        val type = ProtocolConstants.TYPE_RESUME_ACTIVITY

        assertEquals(defaultLifeTime, normalizeAppContextLifeTime(type, null))
        assertEquals(1L, normalizeAppContextLifeTime(type, 1))
        assertEquals(maximumLifeTime, normalizeAppContextLifeTime(type, maximumLifeTime))
        assertEquals(maximumLifeTime, normalizeAppContextLifeTime(type, maximumLifeTime + 1))
    }

    @Test
    fun normalizeAppContextLifeTimeRejectsNonPositiveValues() {
        val type = ProtocolConstants.TYPE_RESUME_ACTIVITY

        listOf(-1L, 0L).forEach { lifeTime ->
            val exception = assertThrows(InvalidParameterException::class.java) {
                normalizeAppContextLifeTime(type, lifeTime)
            }
            assertEquals(
                "lifeTime: must be positive when sending app context",
                exception.message,
            )
        }
    }

    @Test
    fun normalizeAppContextLifeTimePreservesBrowserHistoryRetention() {
        val type = ProtocolConstants.TYPE_BROWSER_HISTORY
        val nonExpiringLifeTime = ProtocolConstants.BROWSER_HISTORY_LIFE_TIME_MILLIS
        val overAppContextLimit = ProtocolConstants.APPCONTEXT_MAX_LIFE_TIME_MILLIS + 1

        assertEquals(nonExpiringLifeTime, normalizeAppContextLifeTime(type, null))
        assertEquals(nonExpiringLifeTime, normalizeAppContextLifeTime(type, nonExpiringLifeTime))
        assertEquals(1L, normalizeAppContextLifeTime(type, 1L))
        assertEquals(overAppContextLimit, normalizeAppContextLifeTime(type, overAppContextLimit))
    }

    @Test
    fun normalizeAppContextLifeTimeRejectsOtherNonPositiveBrowserHistoryValues() {
        listOf(-2L, 0L).forEach { lifeTime ->
            assertThrows(InvalidParameterException::class.java) {
                normalizeAppContextLifeTime(ProtocolConstants.TYPE_BROWSER_HISTORY, lifeTime)
            }
        }
    }
}
