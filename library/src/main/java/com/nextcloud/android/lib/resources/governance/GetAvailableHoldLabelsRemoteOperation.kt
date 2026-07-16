/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.governance

import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.GetMethod
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.resources.OCSRemoteOperation
import kotlinx.serialization.SerializationException
import org.apache.commons.httpclient.HttpStatus
import java.io.IOException

/**
 * Get the hold labels the user may apply to an entity
 */
class GetAvailableHoldLabelsRemoteOperation(
    private val entityType: String,
    private val entityId: Long
) : OCSRemoteOperation<List<HoldLabelInfo>>() {
    override fun run(client: NextcloudClient): RemoteOperationResult<List<HoldLabelInfo>> {
        val getMethod =
            GetMethod(
                client.baseUri.toString() + ENDPOINT + entityType + "/" + entityId +
                    HOLD_AVAILABLE + JSON_FORMAT,
                true
            )
        return try {
            val status = client.execute(getMethod)
            if (status == HttpStatus.SC_OK) {
                val response = governanceJson.decodeFromString<GovernanceOcsResponse<List<HoldLabelInfo>>>(
                    getMethod.getResponseBodyAsString()
                )
                val data = response.ocs.data
                RemoteOperationResult<List<HoldLabelInfo>>(true, getMethod).apply { setResultData(data) }
            } else {
                RemoteOperationResult(false, getMethod)
            }
        } catch (e: SerializationException) {
            failure(e)
        } catch (e: IOException) {
            failure(e)
        } finally {
            getMethod.releaseConnection()
        }
    }

    private fun failure(e: Exception): RemoteOperationResult<List<HoldLabelInfo>> =
        RemoteOperationResult<List<HoldLabelInfo>>(e).also {
            Log_OC.e(TAG, "Get available hold labels failed: " + it.logMessage, it.exception)
        }

    companion object {
        private val TAG = GetAvailableHoldLabelsRemoteOperation::class.java.simpleName
        private const val ENDPOINT = "/ocs/v2.php/apps/governance/v1/labels/"
        private const val HOLD_AVAILABLE = "/hold/available"
    }
}
