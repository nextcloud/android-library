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
import com.owncloud.android.lib.resources.files.ReadFolderRemoteOperation
import org.apache.commons.httpclient.HttpMethodBase
import org.apache.commons.httpclient.HttpStatus
import org.apache.commons.httpclient.methods.DeleteMethod
import org.apache.commons.httpclient.methods.PutMethod

class ToggleEncryptionRemoteOperation
    @JvmOverloads
    constructor(
        private val localId: Long,
        private val remotePath: String?,
        private val encryption: Boolean,
        private val sessionTimeOut: SessionTimeOut = defaultSessionTimeOut
    ) : RemoteOperation<Unit>() {
        @Deprecated("Deprecated in Java")
        @Suppress("Detekt.TooGenericExceptionCaught", "DEPRECATION")
        override fun run(client: OwnCloudClient): RemoteOperationResult<Unit> {
            val folderResult = ReadFolderRemoteOperation(remotePath).execute(client)
            if (folderResult.isSuccess && folderResult.getData().size > 1) {
                return RemoteOperationResult(false, "Non empty", HttpStatus.SC_FORBIDDEN)
            }

            return runCatching { toggleEncryption(client) }
                .getOrElse { e ->
                    Log_OC.e(TAG, "Setting encryption status of $localId failed", e)
                    RemoteOperationResult(e as? Exception ?: RuntimeException(e))
                }
        }

        private fun toggleEncryption(client: OwnCloudClient): RemoteOperationResult<Unit> {
            val (status, method) = executeWithFallback(client)

            return if (status == HttpStatus.SC_OK) {
                RemoteOperationResult<Unit>(true, method)
            } else {
                client.exhaustResponse(method.getResponseBodyAsStream())
                RemoteOperationResult<Unit>(false, method)
            }.also {
                method.releaseConnection()
            }
        }

        /**
         * Tries the v2 endpoint first, falling back to v1 on 404/500.
         * Returns the final status code and the method used.
         */
        private fun executeWithFallback(client: OwnCloudClient): Pair<Int, HttpMethodBase> {
            val v2Method = buildMethod(client, ENCRYPTED_URL_V2)
            val v2Status = client.executeMethod(v2Method, sessionTimeOut.readTimeOut, sessionTimeOut.connectionTimeOut)

            val needsFallback = v2Status == HttpStatus.SC_NOT_FOUND || v2Status == HttpStatus.SC_INTERNAL_SERVER_ERROR
            if (!needsFallback) return v2Status to v2Method

            v2Method.releaseConnection()
            val v1Method = buildMethod(client, ENCRYPTED_URL_V1)
            val v1Status = client.executeMethod(v1Method, sessionTimeOut.readTimeOut, sessionTimeOut.connectionTimeOut)
            return v1Status to v1Method
        }

        private fun buildMethod(
            client: OwnCloudClient,
            baseUrl: String
        ): HttpMethodBase =
            (
                if (encryption) {
                    PutMethod("${client.baseUri}$baseUrl$localId")
                } else {
                    DeleteMethod("${client.baseUri}$baseUrl$localId")
                }
            ).apply {
                addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE)
                addRequestHeader(CONTENT_TYPE, FORM_URLENCODED)
            }

        companion object {
            private val TAG = ToggleEncryptionRemoteOperation::class.java.simpleName
            private const val ENCRYPTED_URL_V1 = "/ocs/v2.php/apps/end_to_end_encryption/api/v1/encrypted/"
            private const val ENCRYPTED_URL_V2 = "/ocs/v2.php/apps/end_to_end_encryption/api/v2/encrypted/"
        }
    }
