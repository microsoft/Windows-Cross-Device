/*
 * Copyright (c) Microsoft Corporation.
 * Licensed under the MIT License
 */
package com.microsoft.crossdevicesdk.continuity

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppContextTest {

    @Before
    fun setUp() {
        // No-op
    }

    @After
    fun tearDown() {
        // No-op
    }

    @Test
    fun appContextValuesTest() {
        val appContext = AppContext()
        val contextId = "com.microsoft.example.xdr.a"
        val packageName = "com.microsoft .crossdevicesdk"
        val webUri = "https://www.microsoft.com/crossdevicesdk.docx"
        val title = "cross device sdk"
        val contextType = ProtocolConstants.TYPE_APPLICATION_CONTEXT
        val lifeTime = 1000L
        val extra =
            "[{\"timestamp\":123,\"browserWebUri\":\"https:\\/\\" +
                "/www.microsoft.com\",\"title\":\"Microsoft\"},{\"timestamp\":456," +
                "\"browserWebUri\":\"https:\\/\\/www.root.com\",\"title\":\"Root\"}," +
                "{\"timestamp\":789,\"browserWebUri\":\"https:\\/\\/www.admin.com\"," +
                "\"title\":\"Admin\",\"favicon\":\"IE8=\\n\"}]"
        appContext.appId = packageName
        appContext.contextId = contextId
        appContext.webLink = webUri
        appContext.title = title
        appContext.lifeTime = lifeTime
        appContext.extras = extra
        appContext.type = contextType

        val values = appContext.contentValues
        assertEquals(packageName, values.getAsString(ProtocolConstants.APPCONTEXT_APP_ID_KEY))
        assertEquals(contextId, values.getAsString(ProtocolConstants.APPCONTEXT_CONTEXT_ID_KEY))
        assertEquals(webUri, values.getAsString(ProtocolConstants.APPCONTEXT_WEBLINK_KEY))
        assertEquals(title, values.getAsString(ProtocolConstants.APPCONTEXT_TITLE_KEY))
        assertEquals(lifeTime, values.getAsLong(ProtocolConstants.APPCONTEXT_LIFE_TIME_KEY))
        assertEquals(
            "[{\"timestamp\":123,\"browserWebUri\":\"https:\\/\\" +
                "/www.microsoft.com\",\"title\":\"Microsoft\"},{\"timestamp\":456," +
                "\"browserWebUri\":\"https:\\/\\/www.root.com\",\"title\":\"Root\"}," +
                "{\"timestamp\":789,\"browserWebUri\":\"https:\\/\\/www.admin.com\"," +
                "\"title\":\"Admin\",\"favicon\":\"IE8=\\n\"}]",
            values.getAsString(ProtocolConstants.APPCONTEXT_EXTRAS_KEY)
        )
    }
}
