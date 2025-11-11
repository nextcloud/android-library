/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2025 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package com.owncloud.android.lib.resources.assistant.chat

import com.google.gson.reflect.TypeToken
import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.PutMethod
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.ocs.ServerResponse
import com.owncloud.android.lib.resources.OCSRemoteOperation
import com.owncloud.android.lib.resources.assistant.chat.model.ChatMessage
import com.owncloud.android.lib.resources.assistant.chat.model.ChatMessageRequest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.apache.commons.httpclient.HttpStatus

class CreateMessageRemoteOperation(
    private val messageRequest: ChatMessageRequest
) : OCSRemoteOperation<ChatMessage>() {
    @Suppress("TooGenericExceptionCaught")
    override fun run(client: NextcloudClient): RemoteOperationResult<ChatMessage> {
        val json = gson.toJson(messageRequest.bodyMap)
        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())

        val putMethod = PutMethod(client.baseUri.toString() + "$BASE_URL/new_message", true, requestBody)
        val status = putMethod.execute(client)

        return try {
            if (status == HttpStatus.SC_OK) {
                val response =
                    getServerResponse(
                        putMethod,
                        object : TypeToken<ServerResponse<ChatMessage>>() {}
                    )
                val result: RemoteOperationResult<ChatMessage> = RemoteOperationResult(true, putMethod)
                result.resultData = response?.ocs?.data
                result
            } else {
                RemoteOperationResult(false, putMethod)
            }
        } catch (e: Exception) {
            Log_OC.e(TAG, "create message: ", e)
            RemoteOperationResult(false, putMethod)
        } finally {
            putMethod.releaseConnection()
        }
    }

    companion object {
        private const val TAG = "CreateMessageRemoteOperation"
        private const val BASE_URL = "/ocs/v2.php/apps/assistant/chat"
    }
}
