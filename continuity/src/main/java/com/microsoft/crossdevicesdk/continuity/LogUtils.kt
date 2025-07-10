/*
 * Copyright (c) Microsoft Corporation.
 * Licensed under the MIT License
 */
package com.microsoft.crossdevicesdk.continuity

import android.util.Log

/**
 * An util class that used for logcat in debug mode.
 */
object LogUtils {
    private var enableLogcat = false

    /**
     * Sets debug mode. Logcat will be enabled if using a debug version of SDK and the param is true.
     *
     * @param isDebugMode debug mode represents that the app want to enable the logcat.
     * @return true if the logcat is enabled.
     */
    fun setDebugMode(isDebugMode: Boolean): Boolean {
        enableLogcat = BuildConfig.DEBUG && isDebugMode
        return enableLogcat
    }

    fun e(
        tag: String,
        msg: String,
        throwable: Throwable,
    ) {
        if (enableLogcat) {
            Log.e(tag, msg, throwable)
        }
    }

    fun e(
        tag: String,
        msg: String,
    ) {
        if (enableLogcat) {
            Log.e(tag, msg)
        }
    }

    fun w(
        tag: String,
        msg: String,
        throwable: Throwable,
    ) {
        if (enableLogcat) {
            Log.w(tag, msg, throwable)
        }
    }

    fun w(
        tag: String,
        msg: String,
    ) {
        if (enableLogcat) {
            Log.w(tag, msg)
        }
    }

    fun i(
        tag: String,
        msg: String,
    ) {
        if (enableLogcat) {
            Log.i(tag, msg)
        }
    }

    fun d(
        tag: String,
        msg: String,
    ) {
        if (enableLogcat) {
            Log.d(tag, msg)
        }
    }

    fun v(
        tag: String,
        msg: String,
    ) {
        if (enableLogcat) {
            Log.v(tag, msg)
        }
    }
}
