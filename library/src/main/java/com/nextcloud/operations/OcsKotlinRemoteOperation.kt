/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.operations

import com.nextcloud.common.NextcloudClient
import com.nextcloud.common.OkHttpMethodBase
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.ocs.OcsKotlinResponse
import com.owncloud.android.lib.ocs.ocsJson
import kotlinx.serialization.KSerializer
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.apache.commons.httpclient.HttpStatus

abstract class OcsKotlinRemoteOperation<T> : RemoteOperation<T>() {
    protected abstract fun createMethod(baseUrl: String): OkHttpMethodBase

    protected abstract fun parse(body: String): T?

    @Suppress("TooGenericExceptionCaught")
    override fun run(client: NextcloudClient): RemoteOperationResult<T> {
        val method = createMethod(client.baseUri.toString())
        return try {
            val status = client.execute(method)
            method.networkException?.let { throw it }
            if (status != HttpStatus.SC_OK) {
                Log_OC.e(tag, "Failed with status code: $status")
                return RemoteOperationResult(false, method)
            }
            val data = parse(method.getResponseBodyAsString())
            RemoteOperationResult<T>(true, method).apply { resultData = data }
        } catch (e: Exception) {
            failure(e)
        } finally {
            method.releaseConnection()
        }
    }

    protected fun <R> decode(
        body: String,
        serializer: KSerializer<R>
    ): R = ocsJson.decodeFromString(OcsKotlinResponse.serializer(serializer), body).ocs.data

    protected fun <R> jsonBody(
        value: R,
        serializer: KSerializer<R>
    ): RequestBody = ocsJson.encodeToString(serializer, value).toRequestBody(JSON_MEDIA_TYPE.toMediaTypeOrNull())

    @Suppress("DEPRECATION")
    private fun failure(e: Exception): RemoteOperationResult<T> =
        RemoteOperationResult<T>(e).also {
            Log_OC.e(tag, "Failed: " + it.logMessage, it.exception)
        }

    private val tag: String
        get() = javaClass.simpleName

    companion object {
        private const val JSON_MEDIA_TYPE = "application/json"
    }
}
