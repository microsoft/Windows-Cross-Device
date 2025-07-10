/*
 * Copyright (c) Microsoft Corporation.
 * Licensed under the MIT License
 */
package com.microsoft.crossdevicesdk.continuity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.XmlResourceParser
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import android.util.Base64
import java.io.IOException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException

internal class AppContextRequestHelper private constructor() {
    init {
        throw UnsupportedOperationException("Utility class and cannot be instantiated")
    }

    companion object {
        private const val TAG = "AppContextRequestHelper"
        private val APPROVED_CONTEXT_REQUESTERS: MutableMap<String?, String> = HashMap()

        fun validateContentProviderAuthority(
            context: Context,
            uri: Uri,
        ): AppContextRequestStatus {
            val packageManager = context.packageManager
            val provider = resolveContentProvider(packageManager, uri)
                ?: return AppContextRequestStatus.INVALID_URI_PROVIDER

            if (APPROVED_CONTEXT_REQUESTERS.isEmpty()) {
                initializeApprovedRequesters(context)
                if (APPROVED_CONTEXT_REQUESTERS.isEmpty()) {
                    return AppContextRequestStatus.INVALID_REQUESTER_PACKAGE
                }
            }
            if (!isCallerApproved(provider.packageName)) {
                return AppContextRequestStatus.INVALID_REQUESTER_PACKAGE
            }

            return verifySignatureDigest(packageManager, provider.packageName)
        }

        private fun resolveContentProvider(packageManager: PackageManager, uri: Uri) =
            packageManager.resolveContentProvider(uri.authority!!, 0)

        private fun isCallerApproved(packageName: String?) =
            APPROVED_CONTEXT_REQUESTERS.keys.contains(packageName)

        private fun verifySignatureDigest(
            packageManager: PackageManager,
            packageName: String
        ): AppContextRequestStatus {
            return try {
                val signatures = getSignatures(packageManager, packageName)
                if (signatures == null) {
                    LogUtils.e(TAG, "signatures must not be null")
                    return AppContextRequestStatus.SIGNATURE_CHECK_FAILED
                }
                val digest = MessageDigest.getInstance("SHA-256")
                for (signature in signatures) {
                    val sha256Digest = toHexString(digest.digest(signature.toByteArray()))
                    if (sha256Digest.contentEquals(APPROVED_CONTEXT_REQUESTERS[packageName])) {
                        return AppContextRequestStatus.VALID
                    }
                }
                handleDebugBuild()
            } catch (e: NoSuchAlgorithmException) {
                LogUtils.e(TAG, "Couldn't get signature digest", e)
                return AppContextRequestStatus.SIGNATURE_CHECK_FAILED
            } catch (e: PackageManager.NameNotFoundException) {
                LogUtils.e(TAG, "Couldn't get signature digest", e)
                return AppContextRequestStatus.SIGNATURE_CHECK_FAILED
            }
        }

        private fun getSignatures(packageManager: PackageManager, packageName: String) =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                ).signingInfo.signingCertificateHistory
            } else {
                packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNATURES
                ).signatures
            }

        private fun handleDebugBuild(): AppContextRequestStatus {
            if (BuildConfig.DEBUG) {
                LogUtils.w(TAG, "validateCaller: Ignoring invalid signature: Debug build")
                return AppContextRequestStatus.VALID
            }
            return AppContextRequestStatus.SIGNATURE_CHECK_FAILED
        }

        private fun initializeApprovedRequesters(context: Context) {
            val parser = context.resources.getXml(R.xml.context_handoff_requesters)
            try {
                var packageName: String? = null
                var currentTag: String? = null
                while (parser.eventType != XmlResourceParser.END_DOCUMENT) {
                    when (parser.eventType) {
                        XmlResourceParser.START_TAG -> {
                            currentTag = parser.name
                            if ("package" == currentTag) {
                                packageName = parser.getAttributeValue(null, "name")
                            }
                        }

                        XmlPullParser.TEXT ->
                            if (!TextUtils.isEmpty(currentTag) && "cert-digest" == currentTag &&
                                !TextUtils.isEmpty(packageName)
                            ) {
                                val digest = parser.text
                                if (!TextUtils.isEmpty(digest)) {
                                    APPROVED_CONTEXT_REQUESTERS[packageName] = digest
                                }
                            }

                        XmlResourceParser.END_TAG ->
                            if (parser.name == "package") {
                                currentTag = null
                                packageName = null
                            }

                        else -> {}
                    }
                    parser.next()
                }
            } catch (e: XmlPullParserException) {
                LogUtils.e(TAG, "Exception loading approved requesters", e)
            } catch (e: IOException) {
                LogUtils.e(TAG, "Exception loading approved requesters", e)
            }
        }

        private fun toHexString(bytes: ByteArray): String {
            val buffer = StringBuilder()
            for (b in bytes) {
                val decimal = b.toInt() and 0xFF
                val hex = Integer.toHexString(decimal)
                if (hex.length % 2 == 1) {
                    buffer.append('0')
                }
                buffer.append(hex)
            }
            return buffer.toString()
        }

        fun convertBrowserHistoryToJsonString(
            browserHistoryList: List<BrowserHistoryItem>
        ): String? {
            val historyArray = JSONArray()
            var index = 0
            for (item in browserHistoryList) {
                val historyObject = createJSONObject()
                try {
                    historyObject.put(
                        ProtocolConstants.APPCONTEXT_BROWSER_TIMESTAMP_KEY,
                        item.timestamp,
                    )
                    historyObject.put(ProtocolConstants.APPCONTEXT_BROWSER_WEB_URI_KEY, item.webUri)
                    historyObject.put(ProtocolConstants.APPCONTEXT_BROWSER_TITLE_KEY, item.title)
                    if (item.getFavIcon() != null) {
                        historyObject.put(
                            ProtocolConstants.APPCONTEXT_BROWSER_FAVICON_KEY,
                            Base64.encodeToString(item.getFavIcon(), Base64.DEFAULT),
                        )
                    }
                    historyArray.put(index++, historyObject)
                } catch (e: JSONException) {
                    LogUtils.e(TAG, "Exception convert to JSON", e)
                    return null
                }
            }
            return historyArray.toString()
        }

        private fun createJSONObject(): JSONObject {
            return JSONObject()
        }

        fun getMataDataInt(
            context: Context,
            key: String,
        ): Int {
            try {
                val appInfo =
                    context.packageManager.getApplicationInfo(
                        context.packageName,
                        PackageManager.GET_META_DATA,
                    )
                val metaData = appInfo.metaData
                return metaData?.getInt(key) ?: ProtocolConstants.DEFAULT_META_DATA_INT
            } catch (e: PackageManager.NameNotFoundException) {
                LogUtils.e(TAG, "Exception getting meta data", e)
            }
            return ProtocolConstants.DEFAULT_META_DATA_INT
        }

        fun registerReceiver(
            context: Context,
            broadcastReceiver: BroadcastReceiver,
            filter: IntentFilter,
            flags: Int
        ): Intent? {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                return context.registerReceiver(
                    broadcastReceiver,
                    filter,
                    null,
                    null,
                    flags
                )
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val newFlags = flags and Context.RECEIVER_VISIBLE_TO_INSTANT_APPS
                return context.registerReceiver(
                    broadcastReceiver,
                    filter,
                    null,
                    null,
                    newFlags
                )
            }
            return context.registerReceiver(broadcastReceiver, filter, null, null)
        }
    }
}
