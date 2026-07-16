/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.governance

import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.DeleteMethod
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.resources.OCSRemoteOperation
import kotlinx.serialization.SerializationException
import org.apache.commons.httpclient.HttpStatus
import java.io.IOException

/**
 * Remove a label from an entity
 */
class RemoveLabelRemoteOperation(
    private val entityType: String,
    private val entityId: Long,
    private val labelType: LabelType,
    private val labelId: String
) : OCSRemoteOperation<GovernanceLabelResponse>() {
    override fun run(client: NextcloudClient): RemoteOperationResult<GovernanceLabelResponse> {
        val deleteMethod =
            DeleteMethod(
                client.baseUri.toString() + ENDPOINT + entityType + "/" + entityId + "/" +
                    labelType.value + "/" + labelId + JSON_FORMAT,
                true
            )
        return try {
            val status = client.execute(deleteMethod)
            if (status == HttpStatus.SC_OK) {
                val response = governanceJson.decodeFromString<GovernanceOcsResponse<GovernanceLabelResponse>>(
                    deleteMethod.getResponseBodyAsString()
                )
                val data = response.ocs.data
                RemoteOperationResult<GovernanceLabelResponse>(true, deleteMethod).apply { resultData = data }
            } else {
                RemoteOperationResult(false, deleteMethod)
            }
        } catch (e: SerializationException) {
            failure(e)
        } catch (e: IOException) {
            failure(e)
        } finally {
            deleteMethod.releaseConnection()
        }
    }

    private fun failure(e: Exception): RemoteOperationResult<GovernanceLabelResponse> =
        RemoteOperationResult<GovernanceLabelResponse>(e).also {
            Log_OC.e(TAG, "Remove label from entity failed: " + it.logMessage, it.exception)
        }

    companion object {
        private val TAG = RemoveLabelRemoteOperation::class.java.simpleName
        private const val ENDPOINT = "/ocs/v2.php/apps/governance/v1/labels/"
    }
}
