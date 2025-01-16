/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2024 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.android.lib.resources.recommendations

import com.google.gson.reflect.TypeToken
import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.GetMethod
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.ocs.ServerResponse
import com.owncloud.android.lib.resources.OCSRemoteOperation
import org.apache.commons.httpclient.HttpStatus

/**
 * Get recommendation of an user
 */
class GetRecommendationsRemoteOperation : OCSRemoteOperation<RecommendationResponse>() {
    @Suppress("TooGenericExceptionCaught")
    override fun run(client: NextcloudClient): RemoteOperationResult<RecommendationResponse> {
        var result: RemoteOperationResult<RecommendationResponse>
        var getMethod: GetMethod? = null
        try {
            getMethod =
                GetMethod(
                    client.baseUri.toString() + ENDPOINT + JSON_FORMAT,
                    true
                )
            val status = client.execute(getMethod)
            if (status == HttpStatus.SC_OK) {
                val map =
                    getServerResponse(
                        getMethod,
                        object : TypeToken<ServerResponse<RecommendationResponse>>() {}
                    )?.ocs?.data

                if (map != null) {
                    result = RemoteOperationResult(true, getMethod)
                    result.setResultData(map)
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
                "Get recommendations failed: " + result.logMessage,
                result.exception
            )
        } finally {
            getMethod?.releaseConnection()
        }
        return result
    }

    companion object {
        private val TAG = GetRecommendationsRemoteOperation::class.java.simpleName
        private const val ENDPOINT = "/ocs/v2.php/apps/recommendations/api/v1/recommendations"
    }
}
