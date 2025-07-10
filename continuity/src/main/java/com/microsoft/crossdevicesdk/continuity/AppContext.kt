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

    var contextId: String
        get() {
            return values[ProtocolConstants.APPCONTEXT_CONTEXT_ID_KEY]?.toString() ?: ""
        }

        /**
         * Sets context id. This is used to distinguish it from other app contexts.
         *
         * This is required and unique for each app context.
         * Format: "${packageName}.${UUID.randomUUID()}"
         * @param contextId the context id
         */
        set(contextId) {
            values[ProtocolConstants.APPCONTEXT_CONTEXT_ID_KEY] = contextId
        }

    var type: Int
        get() {
            val value = values[ProtocolConstants.APPCONTEXT_TYPE_KEY]
            return value?.toString()?.toInt() ?: 0
        }

        /**
         * Sets type. The FLAG indicates which app context type is inserted to LTW.
         *
         * This is required.
         * @param type the type (e.g. [ProtocolConstants.TYPE_BROWSER_HISTORY])
         */
        set(type) {
            values[ProtocolConstants.APPCONTEXT_TYPE_KEY] = type
        }

    var createTime: Long
        get() {
            val value = values[ProtocolConstants.APPCONTEXT_CREATE_TIME_KEY]
            return value?.toString()?.toLong() ?: -1L
        }

        /**
         * Sets create time of the app context.
         *
         * This is required.
         * @param createTime Timestamp representing the the create time
         */
        set(createTime) {
            values[ProtocolConstants.APPCONTEXT_CREATE_TIME_KEY] = createTime
        }

    var lastUpdatedTime: Long
        get() {
            val value = values[ProtocolConstants.APPCONTEXT_LAST_UPDATED_TIME_KEY]
            return value?.toString()?.toLong() ?: -1L
        }

        /**
         * Sets last updated time of the app context.
         *
         * This is required. Any time when any fields of app context is updated, the updated time needs to be recorded.
         * @param lastUpdatedTime Timestamp representing the last updated time
         */
        set(lastUpdatedTime) {
            values[ProtocolConstants.APPCONTEXT_LAST_UPDATED_TIME_KEY] = lastUpdatedTime
        }

    var teamId: String
        get() {
            return values[ProtocolConstants.APPCONTEXT_TEAM_ID_KEY]?.toString() ?: ""
        }

        /**
         * Sets the team id used to identify the organization or group the app belongs to.
         *
         * This is optional.
         * @param teamId the team id
         */
        set(teamId) {
            values[ProtocolConstants.APPCONTEXT_TEAM_ID_KEY] = teamId
        }

    var intentUri: String
        get() {
            return values[ProtocolConstants.APPCONTEXT_INTENT_URL_KEY]?.toString() ?: ""
        }

        /**
         * Sets the intent uri used to indicate which app can continue the app context handed over from the originating device.
         *
         * This is optional.
         * @param intentUri the intent uri
         * @throws IllegalArgumentException if the intent uri exceeds 2083 characters
         */
        set(intentUri) {
            if (intentUri.length > ProtocolConstants.MAX_URI_LENGTH) {
                throw IllegalArgumentException(
                    "intentUri exceeds the maximum length of 2083 characters"
                )
            }
            values[ProtocolConstants.APPCONTEXT_INTENT_URL_KEY] = intentUri
        }

    var appId: String
        get() {
            return values[ProtocolConstants.APPCONTEXT_APP_ID_KEY]?.toString() ?: ""
        }

        /**
         * Sets the package of the application the context is for.
         *
         * This is optional. Note that only context service providers need to use this. If omitted the calling response provider's package will be used.
         * @param appId the package name of the app
         */
        set(appId) {
            values[ProtocolConstants.APPCONTEXT_APP_ID_KEY] = appId
        }

    var title: String
        get() {
            return values[ProtocolConstants.APPCONTEXT_TITLE_KEY]?.toString() ?: ""
        }

        /**
         * Sets the optional, user-visible title for this app context, such as a document name or web page title.
         *
         * This is optional.
         * @param title the title
         */
        set(title) {
            values[ProtocolConstants.APPCONTEXT_TITLE_KEY] = title
        }

    var webLink: String
        get() {
            return values[ProtocolConstants.APPCONTEXT_WEBLINK_KEY]?.toString() ?: ""
        }

        /**
         * Sets web link. This represents the URL of the webpage to load in a browser to continue the app context.
         *
         * This is optional.
         * @param webLink the web link http:// or https:// based URI
         * @throws IllegalArgumentException if the web link exceeds 2083 characters
         */
        set(webLink) {
            if (webLink.length > ProtocolConstants.MAX_URI_LENGTH) {
                throw IllegalArgumentException(
                    "WebLink exceeds the maximum length of 2083 characters"
                )
            }
            values[ProtocolConstants.APPCONTEXT_WEBLINK_KEY] = webLink
        }

    var preview: ByteArray
        get() {
            return values[ProtocolConstants.APPCONTEXT_PREVIEW_KEY] as ByteArray
        }

        /**
         * Sets preview image that can represent the app context.
         *
         * This is optional.
         * @param preview Bytes representing the the preview
         */
        set(preview) {
            values[ProtocolConstants.APPCONTEXT_PREVIEW_KEY] = preview
        }

    var extras: String
        get() {
            return values[ProtocolConstants.APPCONTEXT_EXTRAS_KEY]?.toString() ?: ""
        }

        /**
         * Sets extras. This is a key-value pair object containing app-specific state information needed to continue an app context on the continuing device.
         *
         * This is optional. Need to provide when the app context has its unique data.
         * @param extras the extras
         */
        set(extras) {
            values[ProtocolConstants.APPCONTEXT_EXTRAS_KEY] = extras
        }

    var lifeTime: Long
        get() {
            val value = values[ProtocolConstants.APPCONTEXT_LIFE_TIME_KEY]
            return value?.toString()?.toLong() ?: -1L
        }

        /**
         * Sets life time of the app context.
         *
         * This is optional. Only used for ongoing scenario, if not set, the default value is -1.
         * @param lifeTime the life time in milliseconds
         */
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
