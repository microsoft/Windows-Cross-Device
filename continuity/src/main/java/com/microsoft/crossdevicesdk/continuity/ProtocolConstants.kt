/*
 * Copyright (c) Microsoft Corporation.
 * Licensed under the MIT License
 */
package com.microsoft.crossdevicesdk.continuity

/**
 *
 * Provides constants of type value to the app.
 */
object ProtocolConstants {
    // Broadcast
    internal const val BROADCAST_INTENT_ACTION_FILTER =
        "com.microsoft.crossdevice.appcontextrequest"
    internal const val APPCONTEXT_REQUEST_CONTENTPROVIDER_URI_KEY = "contentProviderUri"
    internal const val ACTION_PARTNER_APP_TRIGGER =
        "com.microsoft.crossdevice.ACTION_PARTNER_APP_TRIGGER"
    internal const val RECEIVER_EXPORTED = 0x2

    // Meta-data
    internal const val TRIGGER_APP_META_DATA = "com.microsoft.crossdevice.trigger.PartnerApp"
    internal const val TRIGGER_SYSTEM_META_DATA = "com.microsoft.crossdevice.trigger.SystemApi"
    internal const val DEFAULT_META_DATA_INT = 0

    // Type for App Handoff feature
    @Deprecated("Legacy type")
    const val TYPE_APPLICATION_CONTEXT = 0x01

    // Type for Browser Continuity feature
    const val TYPE_BROWSER_HISTORY = 0x02
    internal const val TYPE_BROWSER_HISTORY_NAME = "BrowserHistory"

    // Type for XDR feature
    const val TYPE_RESUME_ACTIVITY = 0x04

    val TYPE_MAP =
        mapOf(
            "TYPE_APPLICATION_CONTEXT" to TYPE_APPLICATION_CONTEXT,
            "TYPE_BROWSER_HISTORY" to TYPE_BROWSER_HISTORY,
            "TYPE_RESUME_ACTIVITY" to TYPE_RESUME_ACTIVITY,
        )

    // Connection State
    internal const val CONNECTION_STATE_KEY = "connectionState"
    internal const val CONNECTION_STATE_CONNECTED = 0
    internal const val CONNECTION_STATE_DISCONNECTED = 1

    // Update version to 3.0 as including the resume activity feature
    internal const val DEFAULT_PROTOCOL_VERSION = 3.0
    internal const val LEGACY_PROTOCOL_VERSION = 1.0
    internal const val APP_CONTEXT_PREF = "app_context_pref"

    // Legacy mode
    internal const val USING_LEGACY_MODE = "using_legacy_mode"
    internal const val LOW_VERSION_KEY_URI_TYPES = "uriTypes"

    // App Context
    internal const val APPCONTEXT_VERSION_KEY = "version"
    internal const val APPCONTEXT_CONTEXT_ID_KEY = "contextId"
    internal const val APPCONTEXT_TYPE_KEY = "requestedContextType"
    internal const val APPCONTEXT_TEAM_ID_KEY = "teamId"
    internal const val APPCONTEXT_INTENT_URL_KEY = "intentUri"
    internal const val APPCONTEXT_APP_ID_KEY = "packageName"
    internal const val APPCONTEXT_TITLE_KEY = "title"
    internal const val APPCONTEXT_WEBLINK_KEY = "webLink"
    internal const val APPCONTEXT_PREVIEW_KEY = "preview"
    internal const val APPCONTEXT_EXTRAS_KEY = "extras"
    internal const val APPCONTEXT_CREATE_TIME_KEY = "createTime"
    internal const val APPCONTEXT_LAST_UPDATED_TIME_KEY = "lastUpdatedTime"
    internal const val APPCONTEXT_LIFE_TIME_KEY = "lifeTime"
    internal const val APPCONTEXT_ACTION_KEY = "action"
    internal const val APPCONTEXT_ACTION_UPSERT = "upsert"
    internal const val APPCONTEXT_ACTION_DELETE = "delete"
    internal const val APPCONTEXT_TRIGGER_TYPE_KEY = "triggerType"
    internal const val APPCONTEXT_TRIGGER_TYPE_RECENT_TASK = "RecentTask"
    internal const val APPCONTEXT_TRIGGER_TYPE_APP = "PartnerApp"

    // Extra data
    internal const val EXTRA_PARTNER_PACKAGE = "partnerPackage"

    // Internal
    internal const val MAX_URI_LENGTH = 2083

    // Browser Continuity
    internal const val BROWSER_HISTORY_LIST_MAX_SIZE = 3
    internal const val APPCONTEXT_BROWSER_HISTORY_KEY = "browserHistory"
    internal const val APPCONTEXT_BROWSER_HISTORY_EMPTY_KEY = "browserHistoryEmpty"
    internal const val APPCONTEXT_BROWSER_WEB_URI_KEY = "browserWebUri"
    internal const val APPCONTEXT_BROWSER_TITLE_KEY = "title"
    internal const val APPCONTEXT_BROWSER_TIMESTAMP_KEY = "timestamp"
    internal const val APPCONTEXT_BROWSER_FAVICON_KEY = "favicon"

    internal const val APPCONTEXT_DEFAULT_DAYS = 30L
}
