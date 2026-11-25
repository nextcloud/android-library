/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files

import com.nextcloud.common.HTTPCodes
import com.nextcloud.common.SessionTimeOut
import com.nextcloud.common.SessionTimeOut.Companion.DEFAULT_READ_TIME_OUT
import com.nextcloud.extensions.isTransientFailure
import com.owncloud.android.lib.common.OwnCloudAnonymousCredentials
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.network.RedirectionPath
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.resources.files.model.RetryPolicy
import org.apache.commons.httpclient.methods.HeadMethod

/**
 * Checks for the existence (or absence) of a remote path via HTTP HEAD.
 *
 * Retries automatically on transient transport errors (e.g. "Unrecovered transport
 * exception") using truncated exponential back-off so we don't hammer the server.
 *
 * @param remotePath       DAV path to check.
 * @param successIfAbsent  When `true` a 404 is treated as success (absence check).
 * @param retryPolicy      Controls how many times and how long to retry.
 */
class ExistenceCheckRemoteOperation
    @JvmOverloads
    constructor(
        remotePath: String?,
        private val successIfAbsent: Boolean,
        private val retryPolicy: RetryPolicy = RetryPolicy.DEFAULT
    ) : RemoteOperation<Void>() {
        private val path: String = remotePath.orEmpty()
        private var redirectionPath: RedirectionPath? = null

        fun getRedirectionPath(): RedirectionPath? = redirectionPath

        fun wasRedirected(): Boolean = redirectionPath?.let { it.redirectionsCount > 0 } ?: false

        override fun run(client: OwnCloudClient): RemoteOperationResult<Void> {
            var lastResult = attemptCheck(client)
            var delayMs = retryPolicy.initialDelayMs

            for (attempt in 1..retryPolicy.maxAttempts) {
                if (!lastResult.isTransientFailure()) break

                if (attempt < retryPolicy.maxAttempts) {
                    Log_OC.w(
                        TAG,
                        "Attempt $attempt/${retryPolicy.maxAttempts} failed with " +
                            "${lastResult.httpCode} (${lastResult.logMessage}). " +
                            "Retrying in ${delayMs}ms…"
                    )
                    Thread.sleep(delayMs)
                    delayMs =
                        (delayMs * retryPolicy.backoffMultiplier)
                            .toLong()
                            .coerceAtMost(retryPolicy.maxDelayMs)
                }

                lastResult = attemptCheck(client)
            }

            return lastResult
        }

        @Suppress("TooGenericExceptionCaught")
        private fun attemptCheck(client: OwnCloudClient): RemoteOperationResult<Void> {
            val previousFollowRedirects = client.isFollowRedirects
            var head: HeadMethod? = null

            return try {
                head =
                    if (client.credentials is OwnCloudAnonymousCredentials) {
                        HeadMethod(client.davUri.toString())
                    } else {
                        HeadMethod(client.getFilesDavUri(path))
                    }

                client.isFollowRedirects = false

                val sessionTimeOut = SessionTimeOut(DEFAULT_READ_TIME_OUT, DEFAULT_READ_TIME_OUT)
                head.params.soTimeout = sessionTimeOut.readTimeOut
                var status = client.executeMethod(head, sessionTimeOut.readTimeOut, sessionTimeOut.connectionTimeOut)

                if (previousFollowRedirects) {
                    redirectionPath = client.followRedirection(head)
                    status = redirectionPath!!.lastStatus
                }

                client.exhaustResponse(head.responseBodyAsStream)

                val success =
                    (
                        (
                            status == HTTPCodes.STATUS_OK || status == HTTPCodes.STATUS_UNAUTHORIZED ||
                                status == HTTPCodes.STATUS_FORBIDDEN
                        ) && !successIfAbsent
                    ) ||
                        (status == HTTPCodes.STATUS_NOT_FOUND && successIfAbsent)

                RemoteOperationResult<Void>(
                    success,
                    status,
                    head.statusText,
                    head.responseHeaders
                ).also {
                    Log_OC.d(
                        TAG,
                        "Existence check for ${client.getFilesDavUri(path)} " +
                            "targeting ${if (successIfAbsent) "absence" else "existence"} " +
                            "finished with HTTP $status${if (!success) " (FAIL)" else ""}"
                    )
                }
            } catch (e: Exception) {
                RemoteOperationResult<Void>(e).also {
                    Log_OC.e(
                        TAG,
                        "Existence check for ${client.getFilesDavUri(path)} " +
                            "targeting ${if (successIfAbsent) "absence" else "existence"}: " +
                            it.logMessage,
                        it.exception
                    )
                }
            } finally {
                head?.releaseConnection()
                client.isFollowRedirects = previousFollowRedirects
            }
        }

        companion object {
            private val TAG: String = ExistenceCheckRemoteOperation::class.java.simpleName
        }
    }
