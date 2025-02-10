/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2024 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.android.lib.resources.tos

import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.PostMethod
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.apache.commons.httpclient.HttpStatus

/**
 * Sign terms of services
 */
class SignTermRemoteOperation(
    val id: Int
) : RemoteOperation<Void>() {
    @Suppress("TooGenericExceptionCaught")
    override fun run(client: NextcloudClient): RemoteOperationResult<Void> {
        val requestBody = hashMapOf("termId" to id)

        val json = gson.toJson(requestBody)

        val request = json.toRequestBody("application/json".toMediaTypeOrNull())

        val postMethod = PostMethod(client.baseUri.toString() + ENDPOINT, true, request)

        val status = postMethod.execute(client)

        return if (status == HttpStatus.SC_OK) {
            RemoteOperationResult<Void>(true, postMethod)
        } else {
            RemoteOperationResult<Void>(false, postMethod)
        }
    }

    companion object {
        private const val ENDPOINT = "/ocs/v2.php/apps/terms_of_service/sign"
    }
}
