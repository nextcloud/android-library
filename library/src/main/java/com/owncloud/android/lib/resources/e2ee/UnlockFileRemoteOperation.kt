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
import org.apache.commons.httpclient.methods.DeleteMethod

class UnlockFileRemoteOperation
    @JvmOverloads
    constructor(
        private val localId: Long,
        private val token: String,
        private val sessionTimeOut: SessionTimeOut = defaultSessionTimeOut,
        private val useV2: Boolean = true
    ) : RemoteOperation<Void>() {
        @Deprecated("Deprecated in Java")
        @Suppress("Detekt.TooGenericExceptionCaught")
        override fun run(client: OwnCloudClient): RemoteOperationResult<Void> =
            runCatching { unlockFile(client) }
                .getOrElse { e ->
                    Log_OC.e(TAG, "Unlock file with id $localId failed", e)
                    RemoteOperationResult(e as? Exception ?: RuntimeException(e))
                }

        private fun unlockFile(client: OwnCloudClient): RemoteOperationResult<Void> {
            val (status, deleteMethod) = executeWithFallback(client)

            return RemoteOperationResult<Void>(status == HttpStatus.SC_OK, deleteMethod).also {
                client.exhaustResponse(deleteMethod.responseBodyAsStream)
                deleteMethod.releaseConnection()
            }
        }

        /**
         * Tries the v2 endpoint first, falling back to v1 on 404/500.
         * Returns the final status code and the method used.
         */
        private fun executeWithFallback(client: OwnCloudClient): Pair<Int, DeleteMethod> {
            val v2Method = buildDeleteMethod(client, LOCK_FILE_URL_V2)
            val v2Status = client.executeMethod(v2Method, sessionTimeOut.readTimeOut, sessionTimeOut.connectionTimeOut)

            val needsFallback =
                (!useV2 || v2Status == HttpStatus.SC_NOT_FOUND || v2Status == HttpStatus.SC_INTERNAL_SERVER_ERROR)
            if (!needsFallback) return v2Status to v2Method

            v2Method.releaseConnection()
            val v1Method = buildDeleteMethod(client, LOCK_FILE_URL_V1)
            val v1Status = client.executeMethod(v1Method, sessionTimeOut.readTimeOut, sessionTimeOut.connectionTimeOut)
            return v1Status to v1Method
        }

        private fun buildDeleteMethod(
            client: OwnCloudClient,
            baseUrl: String
        ) = DeleteMethod("${client.baseUri}$baseUrl$localId").apply {
            addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE)
            addRequestHeader(CONTENT_TYPE, FORM_URLENCODED)
            addRequestHeader(E2E_TOKEN, token)
        }

        companion object {
            private val TAG = UnlockFileRemoteOperation::class.java.simpleName
            private const val LOCK_FILE_URL_V1 = "/ocs/v2.php/apps/end_to_end_encryption/api/v1/lock/"
            private const val LOCK_FILE_URL_V2 = "/ocs/v2.php/apps/end_to_end_encryption/api/v2/lock/"
        }
    }
