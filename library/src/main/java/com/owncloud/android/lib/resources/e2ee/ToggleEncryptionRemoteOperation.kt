/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.e2ee

import com.nextcloud.common.NextcloudClient
import com.nextcloud.common.OkHttpMethodBase
import com.nextcloud.common.SessionTimeOut
import com.nextcloud.common.defaultSessionTimeOut
import com.nextcloud.operations.DeleteMethod
import com.nextcloud.operations.PutMethod
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.resources.files.ReadFolderRemoteOperation
import okhttp3.RequestBody.Companion.toRequestBody
import org.apache.commons.httpclient.HttpStatus

class ToggleEncryptionRemoteOperation
    @JvmOverloads
    constructor(
        private val localId: Long,
        private val remotePath: String?,
        private val encryption: Boolean,
        private val sessionTimeOut: SessionTimeOut = defaultSessionTimeOut
    ) : RemoteOperation<Unit>() {
        @Suppress("TooGenericExceptionCaught")
        override fun run(client: NextcloudClient): RemoteOperationResult<Unit> {
            val folderResult = ReadFolderRemoteOperation(remotePath.orEmpty()).execute(client)
            if (folderResult.isSuccess && folderResult.resultData.size > 1) {
                return RemoteOperationResult(false, "Non empty", HttpStatus.SC_FORBIDDEN)
            }

            return runCatching { toggleEncryption(client) }
                .getOrElse { e ->
                    Log_OC.e(TAG, "Setting encryption status of $localId failed", e)
                    RemoteOperationResult(e as? Exception ?: RuntimeException(e))
                }
        }

        private fun toggleEncryption(client: NextcloudClient): RemoteOperationResult<Unit> {
            val (status, method) = executeWithFallback(client)

            return RemoteOperationResult<Unit>(status == HttpStatus.SC_OK, method).also {
                method.releaseConnection()
            }
        }

        /**
         * Tries the v2 endpoint first, falling back to v1 on 404/500.
         * Returns the final status code and the method used.
         */
        private fun executeWithFallback(client: NextcloudClient): Pair<Int, OkHttpMethodBase> {
            val timedClient = client.withSessionTimeOut(sessionTimeOut)

            val v2Method = buildMethod(client, ENCRYPTED_URL_V2)
            val v2Status = timedClient.execute(v2Method)

            val needsFallback = v2Status == HttpStatus.SC_NOT_FOUND || v2Status == HttpStatus.SC_INTERNAL_SERVER_ERROR
            if (!needsFallback) return v2Status to v2Method

            v2Method.releaseConnection()
            val v1Method = buildMethod(client, ENCRYPTED_URL_V1)
            val v1Status = timedClient.execute(v1Method)
            return v1Status to v1Method
        }

        private fun buildMethod(
            client: NextcloudClient,
            baseUrl: String
        ): OkHttpMethodBase {
            val uri = "${client.baseUri}$baseUrl$localId"

            val method =
                if (encryption) {
                    PutMethod(uri, false, "".toRequestBody(null))
                } else {
                    DeleteMethod(uri, false)
                }

            method.addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE)
            method.addRequestHeader(CONTENT_TYPE, FORM_URLENCODED)

            return method
        }

        companion object {
            private val TAG = ToggleEncryptionRemoteOperation::class.java.simpleName
            private const val ENCRYPTED_URL_V1 = "/ocs/v2.php/apps/end_to_end_encryption/api/v1/encrypted/"
            private const val ENCRYPTED_URL_V2 = "/ocs/v2.php/apps/end_to_end_encryption/api/v2/encrypted/"
        }
    }
