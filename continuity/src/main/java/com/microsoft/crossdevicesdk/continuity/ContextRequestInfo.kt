/*
 * Copyright (c) Microsoft Corporation.
 * Licensed under the MIT License
 */
package com.microsoft.crossdevicesdk.continuity

/**
 * Helper class to provide received context information to the partner app.
 */
class ContextRequestInfo {
    private val values: MutableMap<String?, Any> = HashMap()
    var type: Int
        /**
         * Get type. The FLAG indicates which app context type is requested to send.
         * -1 represents invalid type.
         * @return the type (e.g. [ProtocolConstants.TYPE_BROWSER_HISTORY])
         */
        get() {
            val value = values[ProtocolConstants.APPCONTEXT_TYPE_KEY]
            return value?.toString()?.toInt() ?: -1
        }
        internal set(type) {
            values[ProtocolConstants.APPCONTEXT_TYPE_KEY] = type
        }
}
