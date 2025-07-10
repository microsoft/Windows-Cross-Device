/*
 * Copyright (c) Microsoft Corporation.
 * Licensed under the MIT License
 */
package com.microsoft.crossdevicesdk.continuity

import android.content.Context

/**
 * The core API interface that provides the basic functionality.
 */
internal interface IAppContextManager {
    /**
     * Send app context to the host app(Link to Windows). This should be invoked after receiving broadcast from the host app.
     *
     * @param context    Context to send the app context
     * @param appContext the app context
     */
    fun sendAppContext(
        context: Context,
        appContext: AppContext,
        callback: IAppContextResponse,
    )

    /**
     * Delete app context to the host app(Link to Windows). This should be invoked after receiving broadcast from the host app.
     *
     * @param context    Context to send the app context
     * @param contextId the context id used to delete the app context
     * @param callback the callback to get response
     */
    fun deleteAppContext(
        context: Context,
        contextId: String,
        callback: IAppContextResponse,
    )

    /**
     * Initialize the Continuity SDK.
     *
     * @param context   Context to initialize the SDK
     * @param appContextEventHandler    the app context event handler
     */
    fun initialize(
        context: Context,
        appContextEventHandler: IAppContextEventHandler,
    )

    /**
     * DeInitialize the Continuity SDK.
     *
     * @param context   Context to deInitialize the SDK
     */
    fun deInitialize(context: Context)
}
