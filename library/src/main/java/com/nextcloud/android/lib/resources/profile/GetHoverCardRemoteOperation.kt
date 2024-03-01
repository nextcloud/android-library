/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2021-2024 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.android.lib.resources.profile

import com.google.gson.reflect.TypeToken
import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.GetMethod
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.ocs.ServerResponse
import com.owncloud.android.lib.resources.OCSRemoteOperation
import org.apache.commons.httpclient.HttpStatus

/**
 * Get hoverCard of an user
 */
class GetHoverCardRemoteOperation(private val userId: String) : OCSRemoteOperation<HoverCard?>() {
    @Suppress("TooGenericExceptionCaught")
    override fun run(client: NextcloudClient): RemoteOperationResult<HoverCard?> {
        var result: RemoteOperationResult<HoverCard?>
        var getMethod: GetMethod? = null
        try {
            getMethod =
                GetMethod(client.baseUri.toString() + DIRECT_ENDPOINT + userId + JSON_FORMAT, true)
            val status = client.execute(getMethod)
            if (status == HttpStatus.SC_OK) {
                val hoverCard: HoverCard =
                    getServerResponse<ServerResponse<HoverCard>>(
                        getMethod,
                        object : TypeToken<ServerResponse<HoverCard>?>() {}
                    )
                        .ocs.data
                result = RemoteOperationResult(true, getMethod)
                result.setResultData(hoverCard)
            } else {
                result = RemoteOperationResult(false, getMethod)
            }
        } catch (e: Exception) {
            result = RemoteOperationResult(e)
            Log_OC.e(
                TAG,
                "Get hoverCard for user " + userId + " failed: " + result.logMessage,
                result.exception
            )
        } finally {
            getMethod?.releaseConnection()
        }
        return result
    }

    companion object {
        private val TAG = GetHoverCardRemoteOperation::class.java.simpleName
        private const val DIRECT_ENDPOINT = "/ocs/v2.php/hovercard/v1/"
    }
}
