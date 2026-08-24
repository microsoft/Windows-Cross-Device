/*
 * Copyright (c) Microsoft Corporation.
 * Licensed under the MIT License
 */
package com.microsoft.crossdevicesdk.continuity

import android.content.ContentValues
import androidx.collection.ArrayMap

/**
 *
 * Helper class to provide an app context to the host app(Link to Windows).
 */
class AppContext {
    private val values: MutableMap<String?, Any> = ArrayMap()

    /**
     * The context id used to distinguish it from other app contexts.
     *
     * This is required and unique for each app context.
     * Format: "${packageName}.${UUID.randomUUID()}"
     */
    var contextId: String
        get() {
            return values[ProtocolConstants.APPCONTEXT_CONTEXT_ID_KEY]?.toString() ?: ""
        }
        set(contextId) {
            values[ProtocolConstants.APPCONTEXT_CONTEXT_ID_KEY] = contextId
        }

    /**
     * The type flag that indicates which app context type is inserted to LTW.
     *
     * This is required.
     * @see ProtocolConstants.TYPE_BROWSER_HISTORY
     * @see ProtocolConstants.TYPE_RESUME_ACTIVITY
     */
    var type: Int
        get() {
            val value = values[ProtocolConstants.APPCONTEXT_TYPE_KEY]
            return value?.toString()?.toInt() ?: 0
        }
        set(type) {
            values[ProtocolConstants.APPCONTEXT_TYPE_KEY] = type
        }

    /**
     * Unix timestamp in milliseconds representing the create time of the app context.
     *
     * This is required. Suggest using [System.currentTimeMillis].
     */
    var createTime: Long
        get() {
            val value = values[ProtocolConstants.APPCONTEXT_CREATE_TIME_KEY]
            return value?.toString()?.toLong() ?: -1L
        }
        set(createTime) {
            values[ProtocolConstants.APPCONTEXT_CREATE_TIME_KEY] = createTime
        }

    /**
     * Unix timestamp in milliseconds representing the last updated time of the app context.
     *
     * This is required. Suggest using [System.currentTimeMillis].
     * Any time when any fields of app context is updated, the updated time needs to be recorded.
     */
    var lastUpdatedTime: Long
        get() {
            val value = values[ProtocolConstants.APPCONTEXT_LAST_UPDATED_TIME_KEY]
            return value?.toString()?.toLong() ?: -1L
        }
        set(lastUpdatedTime) {
            values[ProtocolConstants.APPCONTEXT_LAST_UPDATED_TIME_KEY] = lastUpdatedTime
        }

    /**
     * The team id used to identify the organization or group the app belongs to.
     *
     * This is optional.
     */
    var teamId: String
        get() {
            return values[ProtocolConstants.APPCONTEXT_TEAM_ID_KEY]?.toString() ?: ""
        }
        set(teamId) {
            values[ProtocolConstants.APPCONTEXT_TEAM_ID_KEY] = teamId
        }

    /**
     * The intent uri used to indicate which app can continue the app context handed over from the originating device.
     *
     * This is optional.
     * @throws IllegalArgumentException if the intent uri exceeds 2083 characters
     */
    var intentUri: String
        get() {
            return values[ProtocolConstants.APPCONTEXT_INTENT_URL_KEY]?.toString() ?: ""
        }
        set(intentUri) {
            if (intentUri.length > ProtocolConstants.MAX_URI_LENGTH) {
                throw IllegalArgumentException(
                    "intentUri exceeds the maximum length of 2083 characters"
                )
            }
            values[ProtocolConstants.APPCONTEXT_INTENT_URL_KEY] = intentUri
        }

    /**
     * The package of the application the context is for.
     *
     * This is optional. Note that only context service providers need to use this. If omitted the calling response provider's package will be used.
     */
    var appId: String
        get() {
            return values[ProtocolConstants.APPCONTEXT_APP_ID_KEY]?.toString() ?: ""
        }
        set(appId) {
            values[ProtocolConstants.APPCONTEXT_APP_ID_KEY] = appId
        }

    /**
     * The optional, user-visible title for this app context, such as a document name or web page title.
     *
     * This is optional.
     */
    var title: String
        get() {
            return values[ProtocolConstants.APPCONTEXT_TITLE_KEY]?.toString() ?: ""
        }
        set(title) {
            values[ProtocolConstants.APPCONTEXT_TITLE_KEY] = title
        }

    /**
     * The URL of the webpage to load in a browser to continue the app context.
     *
     * This is optional. Must be http:// or https:// based URI.
     * @throws IllegalArgumentException if the web link exceeds 2083 characters
     */
    var webLink: String
        get() {
            return values[ProtocolConstants.APPCONTEXT_WEBLINK_KEY]?.toString() ?: ""
        }
        set(webLink) {
            if (webLink.length > ProtocolConstants.MAX_URI_LENGTH) {
                throw IllegalArgumentException(
                    "WebLink exceeds the maximum length of 2083 characters"
                )
            }
            values[ProtocolConstants.APPCONTEXT_WEBLINK_KEY] = webLink
        }

    /**
     * Preview image bytes that can represent the app context.
     *
     * This is optional.
     */
    var preview: ByteArray
        get() {
            return values[ProtocolConstants.APPCONTEXT_PREVIEW_KEY] as ByteArray
        }
        set(preview) {
            values[ProtocolConstants.APPCONTEXT_PREVIEW_KEY] = preview
        }

    /**
     * A key-value pair object containing app-specific state information needed to continue an app context on the continuing device.
     *
     * This is optional. Need to provide when the app context has its unique data.
     */
    var extras: String
        get() {
            return values[ProtocolConstants.APPCONTEXT_EXTRAS_KEY]?.toString() ?: ""
        }
        set(extras) {
            values[ProtocolConstants.APPCONTEXT_EXTRAS_KEY] = extras
        }

    /**
     * The lifetime of the app context in milliseconds.
     *
     * This is optional. For [ProtocolConstants.TYPE_RESUME_ACTIVITY], a missing or
     * greater-than-five-minute value is normalized to five minutes when the app context is sent.
     * An explicit non-positive value rejects the send through
     * [IAppContextResponse.onContextResponseError]. Other app context types retain the 30-day
     * default when this value is missing.
     */
    var lifeTime: Long
        get() {
            val value = values[ProtocolConstants.APPCONTEXT_LIFE_TIME_KEY]
            return value?.toString()?.toLong() ?: -1L
        }
        set(lifeTime) {
            values[ProtocolConstants.APPCONTEXT_LIFE_TIME_KEY] = lifeTime
        }

    internal var action: String
        get() {
            return values[ProtocolConstants.APPCONTEXT_ACTION_KEY]?.toString() ?: ""
        }

        set(action) {
            values[ProtocolConstants.APPCONTEXT_ACTION_KEY] = action
        }

    internal var triggerType: String
        get() {
            return values[ProtocolConstants.APPCONTEXT_TRIGGER_TYPE_KEY]?.toString() ?: ""
        }

        set(triggerType) {
            values[ProtocolConstants.APPCONTEXT_TRIGGER_TYPE_KEY] = triggerType
        }

    internal var version: Double
        get() {
            val value = values[ProtocolConstants.APPCONTEXT_VERSION_KEY]
            return value?.toString()?.toDouble() ?: 0.0
        }

        set(version) {
            values[ProtocolConstants.APPCONTEXT_VERSION_KEY] = version
        }

    internal fun setCustomValue(
        key: String,
        value: Any?,
    ) {
        if (value == null) {
            values.remove(key)
        } else {
            values[key] = value
        }
    }

    internal val contentValues: ContentValues
        get() {
            val contentValues = ContentValues()
            for (key in values.keys) {
                when (val value = values[key]) {
                    is Double -> contentValues.put(key, value as Double?)
                    is Boolean -> contentValues.put(key, value as Boolean?)
                    is Int -> contentValues.put(key, value as Int?)
                    is Long -> contentValues.put(key, value as Long?)
                    is ByteArray -> contentValues.put(key, value as ByteArray?)
                    else -> contentValues.put(key, value.toString())
                }
            }
            return contentValues
        }

    internal fun hasValue(key: String): Boolean {
        return values.containsKey(key)
    }
}
