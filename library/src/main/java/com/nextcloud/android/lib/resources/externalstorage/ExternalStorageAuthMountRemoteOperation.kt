/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.externalstorage

import com.nextcloud.android.lib.resources.externalstorage.model.ExternalStorageAuthRequest
import com.nextcloud.android.lib.resources.externalstorage.model.ExternalStorageAuthResponse
import com.nextcloud.android.lib.resources.externalstorage.model.ExternalStorageCredentials
import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.PutMethod
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.ocs.SEPARATOR
import com.owncloud.android.lib.ocs.ocsJson
import com.owncloud.android.lib.resources.OCSRemoteOperation
import kotlinx.serialization.SerializationException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.apache.commons.httpclient.HttpStatus
import java.io.IOException

/**
 * Sets auth credentials for an external storage of type `password::userprovided` or `password::global::user`
 * Success means the credentials are _stored_, check [ExternalStorageAuthResponse.status] to know if they are _valid_.
 */
class ExternalStorageAuthMountRemoteOperation(
    private val mountId: Long,
    private val credentials: ExternalStorageCredentials
) : OCSRemoteOperation<ExternalStorageAuthResponse>() {
    override fun run(client: NextcloudClient): RemoteOperationResult<ExternalStorageAuthResponse> {
        val body =
            ocsJson
                .encodeToString(ExternalStorageAuthRequest(credentials))
                .toRequestBody(JSON_MEDIA_TYPE.toMediaTypeOrNull())
        val putMethod = PutMethod(client.baseUri.toString() + ENDPOINT + SEPARATOR + mountId, true, body)
        return try {
            val status = client.execute(putMethod)
            if (status != HttpStatus.SC_OK) {
                Log_OC.e(TAG, "Authenticate external storage mount failed with status code: $status")
                return RemoteOperationResult(false, putMethod)
            }
            val data = ocsJson.decodeFromString<ExternalStorageAuthResponse>(putMethod.getResponseBodyAsString())
            RemoteOperationResult<ExternalStorageAuthResponse>(true, putMethod).apply { resultData = data }
        } catch (e: IOException) {
            failure(e)
        } catch (e: SerializationException) {
            failure(e)
        } finally {
            putMethod.releaseConnection()
        }
    }

    private fun failure(e: Exception): RemoteOperationResult<ExternalStorageAuthResponse> =
        RemoteOperationResult<ExternalStorageAuthResponse>(e).also {
            Log_OC.e(TAG, "Authenticate external storage mount failed: $e")
        }

    companion object {
        private val TAG = ExternalStorageAuthMountRemoteOperation::class.java.simpleName
        private const val ENDPOINT = "/index.php/apps/files_external/userglobalstorages"
        private const val JSON_MEDIA_TYPE = "application/json"
    }
}
