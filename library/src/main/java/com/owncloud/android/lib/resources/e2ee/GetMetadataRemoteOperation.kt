/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.e2ee

import com.nextcloud.common.SessionTimeOut
import com.nextcloud.common.defaultSessionTimeOut
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import org.apache.commons.httpclient.HttpStatus
import org.apache.commons.httpclient.methods.GetMethod
import org.json.JSONObject

class GetMetadataRemoteOperation
    @JvmOverloads
    constructor(
        private val fileId: Long,
        private val sessionTimeOut: SessionTimeOut = defaultSessionTimeOut
    ) : RemoteOperation<MetadataResponse>() {
        @Deprecated("Deprecated in Java")
        @Suppress("Detekt.TooGenericExceptionCaught", "DEPRECATION")
        override fun run(client: OwnCloudClient): RemoteOperationResult<MetadataResponse> =
            runCatching { fetchMetadata(client) }
                .getOrElse { e ->
                    Log_OC.e(TAG, "Fetching metadata for folder $fileId failed", e)
                    RemoteOperationResult(e as? Exception ?: RuntimeException(e))
                }

        private fun fetchMetadata(client: OwnCloudClient): RemoteOperationResult<MetadataResponse> {
            val (status, getMethod) = executeWithFallback(client)

            return if (status == HttpStatus.SC_OK) {
                buildSuccessResult(getMethod)
            } else {
                client.exhaustResponse(getMethod.getResponseBodyAsStream())
                RemoteOperationResult(false, getMethod)
            }.also {
                getMethod.releaseConnection()
            }
        }

        /**
         * Tries the v2 endpoint first, falling back to v1 on 404/500.
         * Returns the final status code and the method used.
         */
        private fun executeWithFallback(client: OwnCloudClient): Pair<Int, GetMethod> {
            val v2Method = buildGetMethod(client, METADATA_V2_URL)
            val v2Status = client.executeMethod(v2Method, sessionTimeOut.readTimeOut, sessionTimeOut.connectionTimeOut)

            val needsFallback = v2Status == HttpStatus.SC_NOT_FOUND || v2Status == HttpStatus.SC_INTERNAL_SERVER_ERROR
            if (!needsFallback) return v2Status to v2Method

            v2Method.releaseConnection()
            val v1Method = buildGetMethod(client, METADATA_V1_URL)
            val v1Status = client.executeMethod(v1Method, sessionTimeOut.readTimeOut, sessionTimeOut.connectionTimeOut)
            return v1Status to v1Method
        }

        private fun buildGetMethod(
            client: OwnCloudClient,
            baseUrl: String
        ) = GetMethod("${client.baseUri}$baseUrl$fileId$JSON_FORMAT").apply {
            addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE)
        }

        private fun buildSuccessResult(getMethod: GetMethod): RemoteOperationResult<MetadataResponse> {
            val signature = getMethod.getResponseHeader(HEADER_SIGNATURE)?.value.orEmpty()
            val metadata =
                JSONObject(getMethod.getResponseBodyAsString())
                    .getJSONObject(NODE_OCS)
                    .getJSONObject(NODE_DATA)
                    .getString(NODE_META_DATA)

            return RemoteOperationResult<MetadataResponse>(true, getMethod).apply {
                resultData = MetadataResponse(signature, metadata)
            }
        }

        companion object {
            private val TAG = GetMetadataRemoteOperation::class.java.simpleName
            private const val METADATA_V1_URL = "/ocs/v2.php/apps/end_to_end_encryption/api/v1/meta-data/"
            private const val METADATA_V2_URL = "/ocs/v2.php/apps/end_to_end_encryption/api/v2/meta-data/"
            private const val NODE_OCS = "ocs"
            private const val NODE_DATA = "data"
            private const val NODE_META_DATA = "meta-data"
            private const val HEADER_SIGNATURE = "X-NC-E2EE-SIGNATURE"
        }
    }
