/*
 * Copyright (c) Microsoft Corporation.
 * Licensed under the MIT License
 */
package com.microsoft.crossdevicesdk.continuity

interface IAppContextResponse {
    /**
     * Called when the context was sent to Link to Windows successfully.
     * Note that the callback may not be called on the same thread where the response was provided.
     *
     * @param response AppContext object this callback is for
     */
    fun onContextResponseSuccess(response: AppContext)

    /**
     * Called when there was an error sending the response to Link to Windows.
     * Note that the callback may not be called on the same thread where the response was provided.
     *
     * @param response AppContext object this callback is for
     * @param throwable Exception with details of the error
     */
    fun onContextResponseError(
        response: AppContext,
        throwable: Throwable,
    )
}
