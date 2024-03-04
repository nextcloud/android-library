/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2024 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.assistant

import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.PostMethod
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.apache.commons.httpclient.HttpStatus

class CreateTaskRemoteOperation(private val input: String, private val type: String) :
    RemoteOperation<Void>() {
    override fun run(client: NextcloudClient): RemoteOperationResult<Void> {
        val requestBody =
            hashMapOf(
                "input" to input,
                "type" to type,
                "appId" to "assistant",
                "identifier" to ""
            )

        val json = gson.toJson(requestBody)

        val request = json.toRequestBody("application/json".toMediaTypeOrNull())

        val postMethod = PostMethod(client.baseUri.toString() + TAG_URL, true, request)

        val status = postMethod.execute(client)

        return if (status == HttpStatus.SC_OK) {
            RemoteOperationResult<Void>(true, postMethod)
        } else {
            RemoteOperationResult<Void>(false, postMethod)
        }
    }

    companion object {
        const val TAG_URL = "/ocs/v2.php/textprocessing/schedule"
    }
}
