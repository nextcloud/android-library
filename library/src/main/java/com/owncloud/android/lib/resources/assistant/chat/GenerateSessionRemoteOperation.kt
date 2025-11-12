/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2025 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.assistant.chat

import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.GetMethod
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.resources.assistant.chat.model.SessionTask
import org.apache.commons.httpclient.HttpStatus

class GenerateSessionRemoteOperation(
    private val sessionId: String
) : RemoteOperation<SessionTask>() {

    @Suppress("TooGenericExceptionCaught")
    override fun run(client: NextcloudClient): RemoteOperationResult<SessionTask> {
        val url = client.baseUri.toString() + "$BASE_URL/generate?sessionId=$sessionId"
        val getMethod = GetMethod(url, true)
        val status = getMethod.execute(client)

        return try {
            if (status == HttpStatus.SC_OK) {
                val responseBody = getMethod.getResponseBodyAsString()
                val jsonResponse = gson.fromJson(responseBody, SessionTask::class.java)

                val result = RemoteOperationResult<SessionTask>(true, getMethod)
                result.resultData = jsonResponse
                result
            } else {
                RemoteOperationResult(false, getMethod)
            }
        } catch (e: Exception) {
            Log_OC.e(TAG, "generate request failed: ", e)
            RemoteOperationResult(false, getMethod)
        } finally {
            getMethod.releaseConnection()
        }
    }

    companion object {
        private const val TAG = "GenerateRemoteOperation"
        private const val BASE_URL = "/ocs/v2.php/apps/assistant/chat"
    }
}
