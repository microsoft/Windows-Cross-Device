/*
 * Copyright (c) Microsoft Corporation.
 * Licensed under the MIT License
 */
package com.microsoft.crossdevicesdk.continuity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import java.security.InvalidParameterException

/**
 * Helper class used to implement a broadcast receiver for app context broadcasts.
 */
class AppContextBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (!ProtocolConstants.BROADCAST_INTENT_ACTION_FILTER.contentEquals(intent.action)) {
            LogUtils.w(TAG, "Ignoring broadcast for action: ${intent.action}")
            return
        }
        LogUtils.i(TAG, "Context request broadcast received with intent action ${intent.action}")
        if (isDisconnected(intent)) {
            return
        }

        val contentProviderUriString = getContentProviderUriString(intent)
        if (TextUtils.isEmpty(contentProviderUriString)) {
            handleInvalidContentProviderUri()
            return
        }

        val contentProviderUri = Uri.parse(contentProviderUriString)
        val status = validateContentProviderUri(context, contentProviderUri)
        logValidationResult(contentProviderUri, status)
        if (status != AppContextRequestStatus.VALID) {
            return
        }

        val (usingLegacyMode, requestedContextType) = getLegacyModeAndContextType(intent)
        updateSharedPreferences(
            context,
            requestedContextType,
            contentProviderUriString,
            usingLegacyMode
        )
        handleContextRequestEvent(requestedContextType)
    }

    private fun isDisconnected(intent: Intent): Boolean {
        val connectionState = intent.getIntExtra(
            ProtocolConstants.CONNECTION_STATE_KEY,
            ProtocolConstants.CONNECTION_STATE_CONNECTED
        )
        return if (connectionState == ProtocolConstants.CONNECTION_STATE_DISCONNECTED) {
            AppContextManager.getAppContextEventHandler()?.onSyncServiceDisconnected()
            true
        } else {
            false
        }
    }

    private fun getContentProviderUriString(intent: Intent): String? {
        return intent.getStringExtra(ProtocolConstants.APPCONTEXT_REQUEST_CONTENTPROVIDER_URI_KEY)
    }

    private fun handleInvalidContentProviderUri() {
        val exception =
            InvalidParameterException(
                ProtocolConstants.APPCONTEXT_REQUEST_CONTENTPROVIDER_URI_KEY + ":missing"
            )
        AppContextManager.getAppContextEventHandler()
            ?.onInvalidContextRequestReceived(exception)
            ?: LogUtils.e(TAG, exception.message!!)
    }

    private fun validateContentProviderUri(
        context: Context,
        contentProviderUri: Uri
    ): AppContextRequestStatus {
        return AppContextRequestHelper.validateContentProviderAuthority(context, contentProviderUri)
    }

    private fun logValidationResult(contentProviderUri: Uri, status: AppContextRequestStatus) {
        LogUtils.i(TAG, "Content provider URI: $contentProviderUri [${status.name}]")
        if (status != AppContextRequestStatus.VALID) {
            LogUtils.w(TAG, "Invalid intent extras $contentProviderUri:${status.name}")
        }
    }

    private fun getLegacyModeAndContextType(intent: Intent): Pair<Boolean, Int> {
        val usingLegacyMode = intent.hasExtra(ProtocolConstants.LOW_VERSION_KEY_URI_TYPES)
        val requestedContextType = intent.getIntExtra(
            ProtocolConstants.APPCONTEXT_TYPE_KEY,
            ProtocolConstants.TYPE_BROWSER_HISTORY
        )
        return Pair(usingLegacyMode, requestedContextType)
    }

    private fun updateSharedPreferences(
        context: Context,
        requestedContextType: Int,
        contentProviderUriString: String?,
        usingLegacyMode: Boolean
    ) {
        val preferences =
            context.getSharedPreferences(ProtocolConstants.APP_CONTEXT_PREF, Context.MODE_PRIVATE)
        preferences.edit().putString(requestedContextType.toString(), contentProviderUriString)
            .putInt(ProtocolConstants.APPCONTEXT_TYPE_KEY, requestedContextType)
            .putBoolean(ProtocolConstants.USING_LEGACY_MODE, usingLegacyMode)
            .apply()
    }

    private fun handleContextRequestEvent(requestedContextType: Int) {
        AppContextManager.getAppContextEventHandler()?.let {
            val contextRequestInfo = ContextRequestInfo()
            contextRequestInfo.type = requestedContextType
            it.onContextRequestReceived(contextRequestInfo)
        }
    }

    companion object {
        private const val TAG = "AppContextBroadcastReceiver"
    }
}
