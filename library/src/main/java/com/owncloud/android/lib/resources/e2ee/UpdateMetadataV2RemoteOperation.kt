/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2018-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2018 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.e2ee

import android.util.Log
import com.nextcloud.common.SessionTimeOut
import com.nextcloud.common.defaultSessionTimeOut
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import org.apache.commons.httpclient.HttpStatus
import org.apache.commons.httpclient.NameValuePair
import org.apache.commons.httpclient.methods.PutMethod
import org.apache.commons.httpclient.methods.StringRequestEntity
import org.json.JSONObject
import java.net.URLEncoder

/**
 * Remote operation to update the folder metadata
 */
class UpdateMetadataV2RemoteOperation
    @JvmOverloads
    constructor(
        private val remoteId: String,
        encryptedMetadataJson: String,
        private val token: String,
        private val signature: String,
        private val sessionTimeOut: SessionTimeOut = defaultSessionTimeOut
    ) : RemoteOperation<String>() {
        private val encryptedMetadataJson: String = URLEncoder.encode(encryptedMetadataJson)

        /**
         * @param client Client object
         */
        @Deprecated("Deprecated in Java")
        @Suppress("Detekt.TooGenericExceptionCaught")
        override fun run(client: OwnCloudClient): RemoteOperationResult<String> {
            var putMethod: PutMethod? = null
            var result: RemoteOperationResult<String>
            try {
                // remote request
                putMethod = PutMethod(client.baseUri.toString() + METADATA_URL + remoteId)
                putMethod.addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE)
                putMethod.addRequestHeader(E2E_TOKEN, token)
                putMethod.addRequestHeader(HEADER_SIGNATURE, signature)
                putMethod.addRequestHeader(CONTENT_TYPE, FORM_URLENCODED)
                val putParams = arrayOfNulls<NameValuePair>(1)
                putParams[0] = NameValuePair(FORMAT, "json")
                putMethod.setQueryString(putParams)
                val data =
                    StringRequestEntity(
                        "metaData=$encryptedMetadataJson",
                        "application/json",
                        "UTF-8"
                    )
                putMethod.requestEntity = data
                val status =
                    client
                        .executeMethod(putMethod, sessionTimeOut.readTimeOut, sessionTimeOut.connectionTimeOut)
                if (status == HttpStatus.SC_OK) {
                    val response = putMethod.responseBodyAsString

                    val respJSON = JSONObject(response)
                    val metadata =
                        respJSON
                            .getJSONObject(NODE_OCS)
                            .getJSONObject(NODE_DATA)
                            .getString(NODE_META_DATA)
                    result = RemoteOperationResult(true, putMethod)
                    result.setResultData(metadata)
                } else {
                    result = RemoteOperationResult(false, putMethod)
                    Log.e(
                        TAG,
                        "Updating metadata for folder " + remoteId + " failed: " + putMethod.responseBodyAsString,
                        result.exception
                    )
                    client.exhaustResponse(putMethod.responseBodyAsStream)
                }
            } catch (e: Exception) {
                result = RemoteOperationResult(e)
                Log.e(
                    TAG,
                    "Updating metadata for folder " + remoteId + " failed: " + result.logMessage,
                    result.exception
                )
            } finally {
                putMethod?.releaseConnection()
            }
            return result
        }

        companion object {
            private val TAG = UpdateMetadataV2RemoteOperation::class.java.simpleName
            private const val METADATA_URL = "/ocs/v2.php/apps/end_to_end_encryption/api/v2/meta-data/"
            private const val FORMAT = "format"

            // JSON node names
            private const val NODE_OCS = "ocs"
            private const val NODE_DATA = "data"
            private const val NODE_META_DATA = "meta-data"
            private const val HEADER_SIGNATURE = "X-NC-E2EE-SIGNATURE"
        }
    }
