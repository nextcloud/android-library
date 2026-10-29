/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-FileCopyrightText: 2017 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.users

import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.GetMethod
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import org.apache.commons.httpclient.HttpStatus
import org.json.JSONObject

/**
 * Fetches the end-to-end encryption certificate of a user.
 *
 * Note: although the endpoint is `.../api/v1/public-key` and the response node is `public-keys`,
 * the server returns the user's PEM-encoded X.509 **certificate** (`-----BEGIN CERTIFICATE-----`).
 */
class GetCertificateRemoteOperation
    @JvmOverloads
    constructor(
        private val user: String = ""
    ) : RemoteOperation<String>() {
        @Suppress("TooGenericExceptionCaught")
        override fun run(client: NextcloudClient): RemoteOperationResult<String> {
            val targetUser = user.ifEmpty { client.userId }
            val getMethod = GetMethod(client.baseUri.toString() + CERTIFICATE_URL + JSON_FORMAT, true)
            if (user.isNotEmpty()) {
                getMethod.setQueryString(mapOf(USERS_PARAM to "[\"$user\"]"))
            }

            return try {
                val status = client.execute(getMethod)
                if (status != HttpStatus.SC_OK) {
                    return RemoteOperationResult(false, getMethod)
                }
                val key =
                    JSONObject(getMethod.getResponseBodyAsString())
                        .getJSONObject(NODE_OCS)
                        .getJSONObject(NODE_DATA)
                        .getJSONObject(NODE_PUBLIC_KEYS)
                        .getString(targetUser)
                RemoteOperationResult<String>(true, getMethod).apply { resultData = key }
            } catch (e: Exception) {
                failure(e, targetUser)
            } finally {
                getMethod.releaseConnection()
            }
        }

        @Suppress("DEPRECATION")
        private fun failure(
            e: Exception,
            targetUser: String
        ): RemoteOperationResult<String> =
            RemoteOperationResult<String>(e).also {
                Log_OC.e(TAG, "Fetching of public key failed for user $targetUser: ${it.logMessage}", it.exception)
            }

        companion object {
            private val TAG = GetCertificateRemoteOperation::class.java.simpleName
            private const val CERTIFICATE_URL = "/ocs/v2.php/apps/end_to_end_encryption/api/v1/public-key"
            private const val USERS_PARAM = "users"
            private const val NODE_OCS = "ocs"
            private const val NODE_DATA = "data"
            private const val NODE_PUBLIC_KEYS = "public-keys"
        }
    }
