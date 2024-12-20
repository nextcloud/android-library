/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2024 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.android.lib.resources.tos

import com.google.gson.reflect.TypeToken
import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.GetMethod
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.ocs.ServerResponse
import com.owncloud.android.lib.resources.OCSRemoteOperation
import org.apache.commons.httpclient.HttpStatus

/**
 * Get terms of service of an user
 */
class GetTermsRemoteOperation : OCSRemoteOperation<Terms>() {
    @Suppress("TooGenericExceptionCaught")
    override fun run(client: NextcloudClient): RemoteOperationResult<Terms> {
        var result: RemoteOperationResult<Terms>
        var getMethod: GetMethod? = null
        try {
            getMethod =
                GetMethod(
                    client.baseUri.toString() + ENDPOINT + JSON_FORMAT,
                    true
                )
            val status = client.execute(getMethod)
            if (status == HttpStatus.SC_OK) {
                val terms =
                    getServerResponse(
                        getMethod,
                        object : TypeToken<ServerResponse<Terms>>() {}
                    )?.ocs?.data

                if (terms != null) {
                    result = RemoteOperationResult(true, getMethod)
                    result.setResultData(terms)
                } else {
                    result = RemoteOperationResult(false, getMethod)
                }
            } else {
                result = RemoteOperationResult(false, getMethod)
            }
        } catch (e: Exception) {
            result = RemoteOperationResult(e)
            Log_OC.e(
                TAG,
                "Get terms failed: " + result.logMessage,
                result.exception
            )
        } finally {
            getMethod?.releaseConnection()
        }
        return result
    }

    companion object {
        private val TAG = GetTermsRemoteOperation::class.java.simpleName
        private const val ENDPOINT = "/ocs/v2.php/apps/terms_of_service/terms"
    }
}
