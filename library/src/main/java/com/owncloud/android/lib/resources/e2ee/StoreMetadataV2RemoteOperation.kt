/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2017 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.e2ee

import android.util.Log
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import org.apache.commons.httpclient.HttpStatus
import org.apache.commons.httpclient.methods.Utf8PostMethod
import org.json.JSONObject

/**
 * Remote operation to store the folder metadata
 */
class StoreMetadataV2RemoteOperation(
    private val remoteId: String,
    private val encryptedMetadataJson: String,
    private val token: String,
    private val signature: String
) : RemoteOperation<String>() {
    /**
     * @param client Client object
     */
    @Deprecated("Deprecated in Java")
    @Suppress("Detekt.TooGenericExceptionCaught")
    fun run(client: OwnCloudClient): RemoteOperationResult<String> {
        var postMethod: Utf8PostMethod? = null
        var result: RemoteOperationResult<String>
        try {
            // remote request
            postMethod = Utf8PostMethod(client.baseUri.toString() + METADATA_URL + remoteId + JSON_FORMAT)
            postMethod.addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE)
            postMethod.addRequestHeader(E2E_TOKEN, token)
            postMethod.addRequestHeader(HEADER_SIGNATURE, signature)
            postMethod.setParameter(METADATA, encryptedMetadataJson)
            val status = client.executeMethod(postMethod, SYNC_READ_TIMEOUT, SYNC_CONNECTION_TIMEOUT)
            if (status == HttpStatus.SC_OK) {
                val response = postMethod.responseBodyAsString

                // Parse the response
                val respJSON = JSONObject(response)
                val metadata =
                    respJSON
                        .getJSONObject(NODE_OCS)
                        .getJSONObject(NODE_DATA)
                        .getString(NODE_META_DATA)
                result = RemoteOperationResult(true, postMethod)
                result.setResultData(metadata)
            } else {
                result = RemoteOperationResult(false, postMethod)
                Log.e(
                    TAG,
                    "Storing of metadata for folder " + remoteId + " failed: " + postMethod.responseBodyAsString,
                    result.exception
                )
                client.exhaustResponse(postMethod.responseBodyAsStream)
            }
        } catch (e: Exception) {
            result = RemoteOperationResult(e)
            Log_OC.e(
                TAG,
                "Storing of metadata for folder " + remoteId + " failed: " + result.logMessage,
                result.exception
            )
        } finally {
            postMethod?.releaseConnection()
        }
        return result
    }

    companion object {
        private val TAG = StoreMetadataV2RemoteOperation::class.java.simpleName
        private const val SYNC_READ_TIMEOUT = 40000
        private const val SYNC_CONNECTION_TIMEOUT = 5000
        private const val METADATA_URL = "/ocs/v2.php/apps/end_to_end_encryption/api/v2/meta-data/"
        private const val METADATA = "metaData"
        private const val HEADER_SIGNATURE = "X-NC-E2EE-SIGNATURE"

        // JSON node names
        private const val NODE_OCS = "ocs"
        private const val NODE_DATA = "data"
        private const val NODE_META_DATA = "meta-data"
    }
}
