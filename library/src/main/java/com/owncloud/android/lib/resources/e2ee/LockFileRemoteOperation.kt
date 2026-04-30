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
import org.apache.commons.httpclient.methods.StringRequestEntity
import org.apache.commons.httpclient.methods.Utf8PostMethod
import org.json.JSONObject

class LockFileRemoteOperation(
    private val localId: Long,
    private val counter: Long = DEFAULT_COUNTER,
    private val sessionTimeOut: SessionTimeOut = defaultSessionTimeOut
) : RemoteOperation<String>() {
    @JvmOverloads
    constructor(localId: Long, sessionTimeOut: SessionTimeOut = defaultSessionTimeOut) : this(
        localId,
        DEFAULT_COUNTER,
        sessionTimeOut
    )

    @Deprecated("Deprecated in Java")
    @Suppress("Detekt.TooGenericExceptionCaught", "DEPRECATION")
    override fun run(client: OwnCloudClient): RemoteOperationResult<String> =
        runCatching { lockFile(client) }
            .getOrElse { e ->
                Log_OC.e(TAG, "Lock file with id $localId failed", e)
                RemoteOperationResult(e as? Exception ?: RuntimeException(e))
            }

    private fun lockFile(client: OwnCloudClient): RemoteOperationResult<String> {
        val (status, postMethod) = executeWithFallback(client)

        return if (status == HttpStatus.SC_OK) {
            buildSuccessResult(postMethod)
        } else {
            client.exhaustResponse(postMethod.getResponseBodyAsStream())
            RemoteOperationResult(false, postMethod)
        }.also {
            postMethod.releaseConnection()
        }
    }

    /**
     * Tries the v2 endpoint first, falling back to v1 on 404/500.
     * Returns the final status code and the method used.
     */
    private fun executeWithFallback(client: OwnCloudClient): Pair<Int, Utf8PostMethod> {
        val v2Method = buildPostMethod(client, LOCK_FILE_URL_V2)
        val v2Status = client.executeMethod(v2Method, sessionTimeOut.readTimeOut, sessionTimeOut.connectionTimeOut)

        val needsFallback = v2Status == HttpStatus.SC_NOT_FOUND || v2Status == HttpStatus.SC_INTERNAL_SERVER_ERROR
        if (!needsFallback) return v2Status to v2Method

        v2Method.releaseConnection()
        val v1Method = buildPostMethod(client, LOCK_FILE_URL_V1)
        val v1Status = client.executeMethod(v1Method, sessionTimeOut.readTimeOut, sessionTimeOut.connectionTimeOut)
        return v1Status to v1Method
    }

    private fun buildPostMethod(
        client: OwnCloudClient,
        baseUrl: String
    ) = Utf8PostMethod("${client.baseUri}$baseUrl$localId$JSON_FORMAT").apply {
        addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE)
        addRequestHeader(CONTENT_TYPE, "application/json")
        addRequestHeader("Accept", "application/json, text/plain, */*")
        addRequestHeader(E2EE_SUPPORTED_HEADER, "true")
        addRequestHeader(REQUESTED_WITH_HEADER, "XMLHttpRequest")
        val counter =
            if (counter > 0) {
                counter
            } else {
                DEFAULT_COUNTER
            }
        addRequestHeader(COUNTER_HEADER, counter.toString())
        setRequestEntity(StringRequestEntity("{}", "application/json", "UTF-8"))
    }

    private fun buildSuccessResult(postMethod: Utf8PostMethod): RemoteOperationResult<String> {
        val token =
            JSONObject(postMethod.getResponseBodyAsString())
                .getJSONObject(NODE_OCS)
                .getJSONObject(NODE_DATA)
                .getString(E2E_TOKEN)

        return RemoteOperationResult<String>(true, postMethod).apply {
            resultData = token
        }
    }

    companion object {
        private val TAG = LockFileRemoteOperation::class.java.simpleName
        private const val LOCK_FILE_URL_V1 = "/ocs/v2.php/apps/end_to_end_encryption/api/v1/lock/"
        private const val LOCK_FILE_URL_V2 = "/ocs/v2.php/apps/end_to_end_encryption/api/v2/lock/"
        private const val COUNTER_HEADER = "X-NC-E2EE-COUNTER"
        private const val E2EE_SUPPORTED_HEADER = "x-e2ee-supported"
        private const val REQUESTED_WITH_HEADER = "x-requested-with"
        private const val DEFAULT_COUNTER: Long = 1
        private const val NODE_OCS = "ocs"
        private const val NODE_DATA = "data"
    }
}
