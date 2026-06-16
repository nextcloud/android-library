/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.governance

import com.google.gson.reflect.TypeToken
import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.DeleteMethod
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.ocs.ServerResponse
import com.owncloud.android.lib.resources.OCSRemoteOperation
import org.apache.commons.httpclient.HttpStatus

/**
 * Remove a label from an entity
 */
class RemoveLabelRemoteOperation(
    private val entityType: String,
    private val entityId: Long,
    private val labelType: LabelType,
    private val labelId: String
) : OCSRemoteOperation<GovernanceLabelResponse>() {
    @Suppress("TooGenericExceptionCaught")
    override fun run(client: NextcloudClient): RemoteOperationResult<GovernanceLabelResponse> {
        var result: RemoteOperationResult<GovernanceLabelResponse>
        var deleteMethod: DeleteMethod? = null
        try {
            deleteMethod =
                DeleteMethod(
                    client.baseUri.toString() + ENDPOINT + entityType + "/" + entityId + "/" +
                        labelType.value + "/" + labelId + JSON_FORMAT,
                    true
                )
            val status = client.execute(deleteMethod)
            if (status == HttpStatus.SC_OK) {
                val response =
                    getServerResponse(
                        deleteMethod,
                        object : TypeToken<ServerResponse<GovernanceLabelResponse>>() {}
                    )?.ocs?.data

                if (response != null) {
                    result = RemoteOperationResult(true, deleteMethod)
                    result.resultData = response
                } else {
                    result = RemoteOperationResult(false, deleteMethod)
                }
            } else {
                result = RemoteOperationResult(false, deleteMethod)
            }
        } catch (e: Exception) {
            result = RemoteOperationResult(e)
            Log_OC.e(
                TAG,
                "Remove label from entity failed: " + result.logMessage,
                result.exception
            )
        } finally {
            deleteMethod?.releaseConnection()
        }
        return result
    }

    companion object {
        private val TAG = RemoveLabelRemoteOperation::class.java.simpleName
        private const val ENDPOINT = "/ocs/v2.php/apps/governance/v1/labels/"
    }
}
