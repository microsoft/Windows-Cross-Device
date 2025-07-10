/*
 * Copyright (c) Microsoft Corporation.
 * Licensed under the MIT License
 */
package com.microsoft.crossdevicesdk.continuity

/**
 * This is the IPC callback interface for the partner app to implement.
 */
interface IAppContextEventHandler {
    /**
     * Called when a valid app context request is received. Partner app can send app context after this.
     *
     * @param contextRequestInfo the received context request information object
     */
    fun onContextRequestReceived(contextRequestInfo: ContextRequestInfo)

    /**
     * Called when there's a validation error on a broadcast request received.
     *
     * @param throwable the throwable
     */
    fun onInvalidContextRequestReceived(throwable: Throwable)

    /**
     * Called when LTW and PL are disconnected. Typically, partner app shouldn't send any context
     * after disconnected.
     */
    fun onSyncServiceDisconnected()
}
