/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2025 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package com.owncloud.android.lib.resources.assistant.chat

import com.google.gson.reflect.TypeToken
import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.GetMethod
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.ocs.ServerResponse
import com.owncloud.android.lib.resources.OCSRemoteOperation
import com.owncloud.android.lib.resources.assistant.chat.model.ChatMessage
import org.apache.commons.httpclient.HttpStatus

class GetMessagesRemoteOperation(
    private val sessionId: String
) : OCSRemoteOperation<List<ChatMessage>>() {
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
                val response =
                    getServerResponse(
                        getMethod,
                        object : TypeToken<ServerResponse<List<ChatMessage>>>() {}
                    )
                val result: RemoteOperationResult<List<ChatMessage>> = RemoteOperationResult(true, getMethod)
                result.resultData = response?.ocs?.data
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
