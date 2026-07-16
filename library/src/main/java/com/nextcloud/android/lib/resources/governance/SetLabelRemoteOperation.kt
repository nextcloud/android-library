/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.governance

import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.PostMethod
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.resources.OCSRemoteOperation
import kotlinx.serialization.SerializationException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.apache.commons.httpclient.HttpStatus
import java.io.IOException

/**
 * Apply a label to an entity
 */
class SetLabelRemoteOperation(
    private val entityType: String,
    private val entityId: Long,
    private val labelType: LabelType,
    private val labelId: String
) : OCSRemoteOperation<GovernanceLabelResponse>() {
    override fun run(client: NextcloudClient): RemoteOperationResult<GovernanceLabelResponse> {
        val body = "".toRequestBody("application/json".toMediaTypeOrNull())
        val postMethod =
            PostMethod(
                client.baseUri.toString() + ENDPOINT + entityType + "/" + entityId + "/" +
                    labelType.value + "/" + labelId + JSON_FORMAT,
                true,
                body
            )
        return try {
            val status = client.execute(postMethod)
            if (status == HttpStatus.SC_OK) {
                val response = governanceJson.decodeFromString<GovernanceOcsResponse<GovernanceLabelResponse>>(
                    postMethod.getResponseBodyAsString()
                )
                val data = response.ocs.data
                RemoteOperationResult<GovernanceLabelResponse>(true, postMethod).apply { resultData = data }
            } else {
                RemoteOperationResult(false, postMethod)
            }
        } catch (e: SerializationException) {
            failure(e)
        } catch (e: IOException) {
            failure(e)
        } finally {
            postMethod.releaseConnection()
        }
    }

    private fun failure(e: Exception): RemoteOperationResult<GovernanceLabelResponse> =
        RemoteOperationResult<GovernanceLabelResponse>(e).also {
            Log_OC.e(TAG, "Apply label to entity failed: " + it.logMessage, it.exception)
        }

    companion object {
        private val TAG = SetLabelRemoteOperation::class.java.simpleName
        private const val ENDPOINT = "/ocs/v2.php/apps/governance/v1/labels/"
    }
}
