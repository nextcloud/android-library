/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2025 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.assistant.chat

import com.google.gson.reflect.TypeToken
import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.GetMethod
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.resources.assistant.chat.model.ChatMessage
import org.apache.commons.httpclient.HttpStatus

class GetMessagesRemoteOperation(
    private val sessionId: String
) : RemoteOperation<List<ChatMessage>>() {
    @Suppress("TooGenericExceptionCaught")
    override fun run(client: NextcloudClient): RemoteOperationResult<List<ChatMessage>> {
        val getMethod =
            GetMethod(
                client.baseUri.toString() + "$BASE_URL/messages?sessionId=$sessionId",
                true
            )
        val status = getMethod.execute(client)

        return try {
            if (status == HttpStatus.SC_OK) {
                val responseBody = getMethod.getResponseBodyAsString()
                val type = object : TypeToken<List<ChatMessage>>() {}.type
                val response: List<ChatMessage> = gson.fromJson(responseBody, type)
                val result: RemoteOperationResult<List<ChatMessage>> = RemoteOperationResult(true, getMethod)
                result.resultData = response
                result
            } else {
                RemoteOperationResult(false, getMethod)
            }
        } catch (e: Exception) {
            Log_OC.e(TAG, "get message list: ", e)
            RemoteOperationResult(false, getMethod)
        } finally {
            getMethod.releaseConnection()
        }
    }

    companion object {
        private const val TAG = "GetMessagesRemoteOperation"
        private const val BASE_URL = "/ocs/v2.php/apps/assistant/chat"
    }
}
