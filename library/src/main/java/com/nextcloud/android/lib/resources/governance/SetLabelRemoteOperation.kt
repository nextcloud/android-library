/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.governance

import com.nextcloud.android.lib.resources.governance.model.GovernanceLabelResponse
import com.nextcloud.android.lib.resources.governance.model.LabelType
import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.PostMethod
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.ocs.OcsKotlinResponse
import com.owncloud.android.lib.ocs.SEPARATOR
import com.owncloud.android.lib.ocs.ocsJson
import com.owncloud.android.lib.resources.OCSRemoteOperation
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.apache.commons.httpclient.HttpStatus

class SetLabelRemoteOperation(
    private val entityType: String,
    private val entityId: Long,
    private val labelType: LabelType,
    private val labelId: String
) : OCSRemoteOperation<GovernanceLabelResponse>() {
    @Suppress("TooGenericExceptionCaught")
    override fun run(client: NextcloudClient): RemoteOperationResult<GovernanceLabelResponse> {
        val body = "".toRequestBody("application/json".toMediaTypeOrNull())
        val postMethod =
            PostMethod(
                client.baseUri.toString() + ENDPOINT + entityType + SEPARATOR + entityId + SEPARATOR +
                    labelType.value + SEPARATOR + labelId + JSON_FORMAT,
                true,
                body
            )
        return try {
            val status = client.execute(postMethod)
            if (status != HttpStatus.SC_OK) {
                Log_OC.e(TAG, "Apply label to entity failed with status code: $status")
                return RemoteOperationResult(false, postMethod)
            }
            val response = ocsJson.decodeFromString<OcsKotlinResponse<GovernanceLabelResponse>>(
                postMethod.getResponseBodyAsString()
            )
            val data = response.ocs.data
            RemoteOperationResult<GovernanceLabelResponse>(true, postMethod).apply { resultData = data }
        } catch (e: Exception) {
            failure(e)
        } finally {
            postMethod.releaseConnection()
        }
    }

    @Suppress("DEPRECATION")
    private fun failure(e: Exception): RemoteOperationResult<GovernanceLabelResponse> =
        RemoteOperationResult<GovernanceLabelResponse>(e).also {
            Log_OC.e(TAG, "Apply label to entity failed: " + it.logMessage, it.exception)
        }

    companion object {
        private val TAG = SetLabelRemoteOperation::class.java.simpleName
        private const val ENDPOINT = "/ocs/v2.php/apps/governance/v1/labels/"
    }
}
