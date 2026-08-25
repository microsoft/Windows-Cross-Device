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

        assertEquals(defaultLifeTime, normalizeAppContextLifeTime(null))
        assertEquals(1L, normalizeAppContextLifeTime(1))
        assertEquals(maximumLifeTime, normalizeAppContextLifeTime(maximumLifeTime))
        assertEquals(maximumLifeTime, normalizeAppContextLifeTime(maximumLifeTime + 1))
    }

    @Test
    fun normalizeAppContextLifeTimeRejectsNonPositiveValues() {
        listOf(-1L, 0L).forEach { lifeTime ->
            val exception = assertThrows(InvalidParameterException::class.java) {
                normalizeAppContextLifeTime(lifeTime)
            }
            assertEquals(
                "lifeTime: must be positive when sending app context",
                exception.message,
            )
        }
    }
}
