/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.governance

import com.google.gson.reflect.TypeToken
import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.PostMethod
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.ocs.ServerResponse
import com.owncloud.android.lib.resources.OCSRemoteOperation
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.apache.commons.httpclient.HttpStatus

/**
 * Apply a label to an entity
 */
class SetLabelRemoteOperation(
    private val entityType: String,
    private val entityId: Long,
    private val labelType: LabelType,
    private val labelId: String
) : OCSRemoteOperation<GovernanceLabelResponse>() {
    @Suppress("TooGenericExceptionCaught")
    override fun run(client: NextcloudClient): RemoteOperationResult<GovernanceLabelResponse> {
        var result: RemoteOperationResult<GovernanceLabelResponse>
        var postMethod: PostMethod? = null
        try {
            val body = "".toRequestBody("application/json".toMediaTypeOrNull())
            postMethod =
                PostMethod(
                    client.baseUri.toString() + ENDPOINT + entityType + "/" + entityId + "/" +
                        labelType.value + "/" + labelId + JSON_FORMAT,
                    true,
                    body
                )
            val status = client.execute(postMethod)
            if (status == HttpStatus.SC_OK) {
                val response =
                    getServerResponse(
                        postMethod,
                        object : TypeToken<ServerResponse<GovernanceLabelResponse>>() {}
                    )?.ocs?.data

                if (response != null) {
                    result = RemoteOperationResult(true, postMethod)
                    result.resultData = response
                } else {
                    result = RemoteOperationResult(false, postMethod)
                }
            } else {
                result = RemoteOperationResult(false, postMethod)
            }
        } catch (e: Exception) {
            result = RemoteOperationResult(e)
            Log_OC.e(
                TAG,
                "Apply label to entity failed: " + result.logMessage,
                result.exception
            )
        } finally {
            postMethod?.releaseConnection()
        }
        return result
    }

    companion object {
        private val TAG = SetLabelRemoteOperation::class.java.simpleName
        private const val ENDPOINT = "/ocs/v2.php/apps/governance/v1/labels/"
    }
}
