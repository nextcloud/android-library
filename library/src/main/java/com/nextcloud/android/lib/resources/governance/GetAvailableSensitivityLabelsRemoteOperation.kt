/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.governance

import com.nextcloud.android.lib.resources.governance.model.SensitivityLabelInfo
import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.GetMethod
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.ocs.OcsKotlinResponse
import com.owncloud.android.lib.ocs.SEPARATOR
import com.owncloud.android.lib.ocs.ocsJson
import com.owncloud.android.lib.resources.OCSRemoteOperation
import org.apache.commons.httpclient.HttpStatus

class GetAvailableSensitivityLabelsRemoteOperation(
    private val entityType: String,
    private val entityId: Long
) : OCSRemoteOperation<List<SensitivityLabelInfo>>() {
    @Suppress("TooGenericExceptionCaught")
    override fun run(client: NextcloudClient): RemoteOperationResult<List<SensitivityLabelInfo>> {
        val getMethod =
            GetMethod(
                client.baseUri.toString() + ENDPOINT + entityType + SEPARATOR + entityId +
                    SENSITIVITY_AVAILABLE + JSON_FORMAT,
                true
            )
        return try {
            val status = client.execute(getMethod)
            if (status != HttpStatus.SC_OK) {
                return RemoteOperationResult(false, getMethod)
            }
            val response = ocsJson.decodeFromString<OcsKotlinResponse<List<SensitivityLabelInfo>>>(
                getMethod.getResponseBodyAsString()
            )
            val data = response.ocs.data
            RemoteOperationResult<List<SensitivityLabelInfo>>(true, getMethod).apply { resultData = data }
        } catch (e: Exception) {
            failure(e)
        } finally {
            getMethod.releaseConnection()
        }
    }

    @Suppress("DEPRECATION")
    private fun failure(e: Exception): RemoteOperationResult<List<SensitivityLabelInfo>> =
        RemoteOperationResult<List<SensitivityLabelInfo>>(e).also {
            Log_OC.e(TAG, "Get available sensitivity labels failed: " + it.logMessage, it.exception)
        }

    companion object {
        private val TAG = GetAvailableSensitivityLabelsRemoteOperation::class.java.simpleName
        private const val ENDPOINT = "/ocs/v2.php/apps/governance/v1/labels/"
        private const val SENSITIVITY_AVAILABLE = "/sensitivity/available"
    }
}
