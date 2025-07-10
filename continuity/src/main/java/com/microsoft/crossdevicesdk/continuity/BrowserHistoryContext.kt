/*
 * Copyright (c) Microsoft Corporation.
 * Licensed under the MIT License
 */
package com.microsoft.crossdevicesdk.continuity

import android.content.Context
import android.net.Uri
import java.util.Locale
import org.json.JSONException
import org.json.JSONObject

/**
 *
 * Helper class to provide browser history context to the host app(Link to Windows).
 */
class BrowserHistoryContext {
    private val browserHistoryList: MutableList<BrowserHistoryItem> = ArrayList()
    private var appId: String? = null
    private var clearBrowserHistory = false

    /**
     * Set browser context empty flag.
     *
     * @param isEmpty Flag to indicate if browser context is empty
     */
    fun setBrowserContextEmptyFlag(isEmpty: Boolean) {
        clearBrowserHistory = isEmpty
    }

    /**
     * Add browsing history context for the app. Each time called will add one uri. Currently up to 3 uris can be provided for context. If more are provided they will be ignored.
     *
     * The uri will be opened in the default browser on Phone Link if clicked.
     *
     * @param timestamp Timestamp representing the time the uri was first opened (or last refreshed)
     * @param webUri The uri (http: or https:)
     * @param title The title of the context
     * @param favIcon [optional] Bytes representing the favIcon. This should be small in general
     */
    fun addBrowserContext(
        timestamp: Long,
        webUri: Uri,
        title: String,
        favIcon: ByteArray?,
    ) {
        if (browserHistoryList.size >= ProtocolConstants.BROWSER_HISTORY_LIST_MAX_SIZE) {
            LogUtils.e(TAG, "Only up to 3 uris supported")
            return
        }
        val logUriMessage = getLogUriMessage(webUri)
        if (isInvalidWebUriOrTitle(webUri, title)) {
            logInvalidWebUriOrTitle(logUriMessage, title)
        } else if (!isSecureWebUri(webUri)) {
            LogUtils.e(TAG, "WebUri is not secure! uri:$logUriMessage")
        } else {
            browserHistoryList.add(BrowserHistoryItem(timestamp, webUri, title, favIcon))
        }
    }

    private fun getLogUriMessage(webUri: Uri): String {
        return if (BuildConfig.DEBUG) {
            webUri.toString()
        } else {
            webUri.toString().length.toString() + " characters"
        }
    }

    private fun isInvalidWebUriOrTitle(webUri: Uri, title: String): Boolean {
        return webUri.toString().isEmpty() || title.isEmpty()
    }

    private fun logInvalidWebUriOrTitle(logUriMessage: String, title: String) {
        LogUtils.e(
            TAG,
            "WebUri and title must not be empty! uri:" +
                "$logUriMessage" +
                " title:" +
                if (BuildConfig.DEBUG) title else "${title.length}" +
                    " characters"
        )
    }

    private fun isSecureWebUri(webUri: Uri): Boolean {
        return webUri.toString().lowercase(Locale.getDefault()).startsWith("http")
    }

    /**
     * Set the package of the application the context is for.
     *
     * This is optional. Note that only context service providers need to use this. If omitted the calling response provider's package will be used.
     * @param appId the package name of the app
     */
    fun setAppId(appId: String) {
        this.appId = appId
    }

    internal fun convertToAppContext(context: Context): AppContext {
        val appContext = AppContext()
        val preferences =
            context.getSharedPreferences(ProtocolConstants.APP_CONTEXT_PREF, Context.MODE_PRIVATE)
        val usingLegacyMode = preferences.getBoolean(ProtocolConstants.USING_LEGACY_MODE, false)
        if (usingLegacyMode) {
            setLegacyModeCustomValues(appContext)
        } else {
            setModernModeCustomValues(appContext)
        }
        setAppContextRequiredValues(context, appContext)
        return appContext
    }

    private fun setLegacyModeCustomValues(appContext: AppContext) {
        if (browserHistoryList.isNotEmpty()) {
            appContext.setCustomValue(
                ProtocolConstants.APPCONTEXT_BROWSER_HISTORY_KEY,
                AppContextRequestHelper.convertBrowserHistoryToJsonString(browserHistoryList),
            )
        }
        if (clearBrowserHistory) {
            appContext.setCustomValue(
                ProtocolConstants.APPCONTEXT_BROWSER_HISTORY_EMPTY_KEY,
                true,
            )
        }
    }

    private fun setModernModeCustomValues(appContext: AppContext) {
        val jsonObject = JSONObject()
        if (browserHistoryList.isNotEmpty()) {
            try {
                jsonObject.put(
                    ProtocolConstants.APPCONTEXT_BROWSER_HISTORY_KEY,
                    AppContextRequestHelper.convertBrowserHistoryToJsonString(browserHistoryList),
                )
            } catch (e: JSONException) {
                LogUtils.w(TAG, "JSON Exception ${e.message} when converting browser history")
            }
        }
        if (clearBrowserHistory) {
            try {
                jsonObject.put(
                    ProtocolConstants.APPCONTEXT_BROWSER_HISTORY_EMPTY_KEY,
                    true,
                )
            } catch (e: JSONException) {
                LogUtils.w(
                    TAG,
                    "JSON Exception ${e.message} when converting browser history empty flag"
                )
            }
        }
        appContext.setCustomValue(
            ProtocolConstants.APPCONTEXT_EXTRAS_KEY,
            jsonObject.toString(),
        )
        appId?.let {
            appContext.appId = it
        }
    }

    private fun setAppContextRequiredValues(context: Context, appContext: AppContext) {
        appContext.type = ProtocolConstants.TYPE_BROWSER_HISTORY
        appContext.contextId =
            "${context.packageName}.${ProtocolConstants.TYPE_BROWSER_HISTORY_NAME}"
        appContext.createTime = System.currentTimeMillis()
        appContext.lastUpdatedTime = System.currentTimeMillis()
    }

    companion object {
        private const val TAG = "BrowserHistoryContext"
    }
}
