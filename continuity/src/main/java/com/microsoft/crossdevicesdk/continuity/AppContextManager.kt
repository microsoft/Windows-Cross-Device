/*
 * Copyright (c) Microsoft Corporation.
 * Licensed under the MIT License
 */
package com.microsoft.crossdevicesdk.continuity

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.net.Uri
import java.lang.ref.WeakReference
import java.security.InvalidParameterException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * The core API implementation that provides the basic functionality.
 * Used to hold the event handler and send app context.
 */
object AppContextManager : IAppContextManager {
    private const val TAG = "AppContextManager"
    private var appContextEventHandler: WeakReference<IAppContextEventHandler>? = null
    private val appContextBroadcastReceiver = AppContextBroadcastReceiver()
    private val coroutineScope by lazy { CoroutineScope(Dispatchers.IO) }

    fun sendAppContext(
        context: Context,
        browserHistoryContext: BrowserHistoryContext,
        callback: IAppContextResponse,
    ) {
        sendAppContext(context, browserHistoryContext.convertToAppContext(context), callback)
    }

    override fun sendAppContext(
        context: Context,
        appContext: AppContext,
        callback: IAppContextResponse,
    ) {
        insertAppContext(context, appContext, callback, ProtocolConstants.APPCONTEXT_ACTION_UPSERT)
    }

    override fun deleteAppContext(
        context: Context,
        contextId: String,
        callback: IAppContextResponse,
    ) {
        val appContext = AppContext().apply {
            this.contextId = contextId
        }
        insertAppContext(context, appContext, callback, ProtocolConstants.APPCONTEXT_ACTION_DELETE)
    }

    private fun insertAppContext(
        context: Context,
        appContext: AppContext,
        callback: IAppContextResponse,
        action: String,
    ) {
        val preferences =
            context.getSharedPreferences(ProtocolConstants.APP_CONTEXT_PREF, Context.MODE_PRIVATE)
        val responseCallback = WeakReference(callback)
        val contentProviderUriString =
            getAppContextUri(preferences, appContext) ?: throw InvalidParameterException(
                ProtocolConstants.APPCONTEXT_REQUEST_CONTENTPROVIDER_URI_KEY +
                    ":missing when sending app context"
            )

        runCatching {
            checkRequiredValues(context, appContext, action)
        }.onFailure { e ->
            LogUtils.e(TAG, e.message!!, e)
            responseCallback.get()?.onContextResponseError(appContext, e)
            return
        }

        appContext.action = action

        setAppContextTriggerType(preferences, appContext)

        setAppContextVersion(preferences, appContext)

        addDefaultValue(context, appContext)

        coroutineScope.launch {
            runCatching {
                context.contentResolver.insert(
                    Uri.parse(contentProviderUriString), appContext.contentValues
                )
            }.onSuccess { responseUri ->
                if (responseUri == null) {
                    LogUtils.w(TAG, "Response not accepted, requester returned a null URI")
                    responseCallback.get()?.onContextResponseError(
                        appContext,
                        UnknownError("Response not accepted, requester returned a null URI")
                    )
                } else {
                    LogUtils.i(TAG, "Response sent successfully")
                    responseCallback.get()?.onContextResponseSuccess(appContext)
                }
            }.onFailure { e ->
                LogUtils.e(TAG, e.message!!, e)
                responseCallback.get()?.onContextResponseError(appContext, e)
            }
        }
    }

    override fun initialize(
        context: Context,
        appContextEventHandler: IAppContextEventHandler,
    ) {
        setAppContextEventHandler(appContextEventHandler)
        AppContextRequestHelper.registerReceiver(
            context.applicationContext,
            appContextBroadcastReceiver,
            filter(),
            ProtocolConstants.RECEIVER_EXPORTED
        )
        sendTriggerIfNeeded(context.applicationContext)
    }

    override fun deInitialize(context: Context) {
        setAppContextEventHandler(null)
        context.applicationContext.unregisterReceiver(appContextBroadcastReceiver)
    }

    private fun setAppContextEventHandler(appContextEventHandler: IAppContextEventHandler?) {
        AppContextManager.appContextEventHandler = WeakReference(appContextEventHandler)
    }

    internal fun getAppContextEventHandler(): IAppContextEventHandler? {
        return appContextEventHandler?.get()
    }

    private fun addDefaultValue(
        context: Context,
        appContext: AppContext,
    ) {
        appContext.lifeTime =
            appContext.takeIf { !it.hasValue(ProtocolConstants.APPCONTEXT_LIFE_TIME_KEY) }
                ?.let {
                    TimeUnit.MILLISECONDS.convert(
                        ProtocolConstants.APPCONTEXT_DEFAULT_DAYS, TimeUnit.DAYS
                    )
                } ?: appContext.lifeTime

        appContext.appId =
            appContext.takeIf { !it.hasValue(ProtocolConstants.APPCONTEXT_APP_ID_KEY) }
                ?.let { context.packageName } ?: appContext.appId
    }

    private fun setAppContextVersion(
        preferences: SharedPreferences,
        appContext: AppContext,
    ) {
        val usingLegacyMode = preferences.getBoolean(ProtocolConstants.USING_LEGACY_MODE, false)
        val version = if (usingLegacyMode) {
            ProtocolConstants.LEGACY_PROTOCOL_VERSION
        } else {
            ProtocolConstants.DEFAULT_PROTOCOL_VERSION
        }
        appContext.version = version
    }

    private fun setAppContextTriggerType(
        preferences: SharedPreferences,
        appContext: AppContext,
    ) {
        val triggerType = preferences.getString(
            appContext.type.toString() + ProtocolConstants.APPCONTEXT_TRIGGER_TYPE_KEY,
            ProtocolConstants.APPCONTEXT_TRIGGER_TYPE_RECENT_TASK,
        )
        appContext.triggerType = triggerType!!
    }

    private fun getAppContextUri(
        preferences: SharedPreferences,
        appContext: AppContext,
    ): String? {
        return if (appContext.type != 0) {
            preferences.getString(appContext.type.toString(), null)
        } else {
            val requestedContextType = preferences.getInt(
                ProtocolConstants.APPCONTEXT_TYPE_KEY, ProtocolConstants.TYPE_BROWSER_HISTORY
            )
            appContext.type = requestedContextType
            preferences.getString(requestedContextType.toString(), null)
        }
    }

    private fun getRequiredFields(action: String): Array<String> {
        return when (action) {
            ProtocolConstants.APPCONTEXT_ACTION_DELETE -> arrayOf(
                ProtocolConstants.APPCONTEXT_TYPE_KEY, ProtocolConstants.APPCONTEXT_CONTEXT_ID_KEY
            )

            else -> arrayOf(
                ProtocolConstants.APPCONTEXT_TYPE_KEY,
                ProtocolConstants.APPCONTEXT_CONTEXT_ID_KEY,
                ProtocolConstants.APPCONTEXT_CREATE_TIME_KEY,
                ProtocolConstants.APPCONTEXT_LAST_UPDATED_TIME_KEY
            )
        }
    }

    private fun checkRequiredValues(context: Context, appContext: AppContext, action: String) {
        getRequiredFields(action).forEach {
            if (!appContext.hasValue(it)) {
                throw InvalidParameterException("$it:missing when sending app context")
            }
        }
        appContext.contextId.startsWith(context.applicationContext.packageName).not().let {
            if (it) {
                throw InvalidParameterException(
                    ProtocolConstants.APPCONTEXT_CONTEXT_ID_KEY +
                        ":invalid when sending app context"
                )
            }
        }
    }

    private fun filter(): IntentFilter {
        return IntentFilter().apply {
            addAction(ProtocolConstants.BROADCAST_INTENT_ACTION_FILTER)
        }
    }

    private fun sendTriggerIfNeeded(applicationContext: Context) {
        val triggerFeatures = AppContextRequestHelper.getMataDataInt(
            applicationContext,
            ProtocolConstants.TRIGGER_APP_META_DATA,
        )
        triggerFeatures.let {
            val preferences = applicationContext.getSharedPreferences(
                ProtocolConstants.APP_CONTEXT_PREF,
                Context.MODE_PRIVATE,
            )
            var needTrigger = false
            ProtocolConstants.TYPE_MAP.keys.forEach { feature ->
                val triggerType =
                    if (
                        triggerFeatures and ProtocolConstants.TYPE_MAP[feature]!! ==
                        ProtocolConstants.TYPE_MAP[feature]
                    ) {
                        needTrigger = true
                        ProtocolConstants.APPCONTEXT_TRIGGER_TYPE_APP
                    } else {
                        ProtocolConstants.APPCONTEXT_TRIGGER_TYPE_RECENT_TASK
                    }
                preferences.edit().putString(
                    "${ProtocolConstants.TYPE_MAP[feature]}" +
                        ProtocolConstants.APPCONTEXT_TRIGGER_TYPE_KEY,
                    triggerType
                ).apply()
            }
            if (needTrigger) {
                Intent().apply {
                    action = ProtocolConstants.ACTION_PARTNER_APP_TRIGGER
                    putExtra(
                        ProtocolConstants.EXTRA_PARTNER_PACKAGE, applicationContext.packageName
                    )
                    applicationContext.sendBroadcast(this)
                }
                LogUtils.i(TAG, "Sent trigger broadcast")
            }
        }
    }
}
