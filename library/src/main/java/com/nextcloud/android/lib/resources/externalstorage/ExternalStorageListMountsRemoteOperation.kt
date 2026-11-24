/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.externalstorage

import com.nextcloud.android.lib.resources.externalstorage.model.ExternalStorageMount
import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.GetMethod
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.ocs.OcsKotlinResponse
import com.owncloud.android.lib.ocs.ocsJson
import com.owncloud.android.lib.resources.OCSRemoteOperation
import kotlinx.serialization.SerializationException
import org.apache.commons.httpclient.HttpStatus
import java.io.IOException

class ExternalStorageListMountsRemoteOperation : OCSRemoteOperation<List<ExternalStorageMount>>() {
    override fun run(client: NextcloudClient): RemoteOperationResult<List<ExternalStorageMount>> {
        val getMethod = GetMethod(client.baseUri.toString() + ENDPOINT + JSON_FORMAT, true)
        return try {
            val status = client.execute(getMethod)
            if (status != HttpStatus.SC_OK) {
                Log_OC.e(TAG, "Get external storage mounts failed with status code: $status")
                return RemoteOperationResult(false, getMethod)
            }
            val response =
                ocsJson.decodeFromString<OcsKotlinResponse<List<ExternalStorageMount>>>(
                    getMethod.getResponseBodyAsString()
                )
            val data = response.ocs.data
            RemoteOperationResult<List<ExternalStorageMount>>(true, getMethod).apply { resultData = data }
        } catch (e: IOException) {
            failure(e)
        } catch (e: SerializationException) {
            failure(e)
        } finally {
            getMethod.releaseConnection()
        }
    }

    private fun failure(e: Exception): RemoteOperationResult<List<ExternalStorageMount>> =
        RemoteOperationResult<List<ExternalStorageMount>>(e).also {
            Log_OC.e(TAG, "Get external storage mounts failed: $e")
        }

    companion object {
        private val TAG = ExternalStorageListMountsRemoteOperation::class.java.simpleName
        private const val ENDPOINT = "/ocs/v2.php/apps/files_external/api/v1/mounts"
    }
}
