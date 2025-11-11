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
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.resources.assistant.chat.model.CreateConversation
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.apache.commons.httpclient.HttpStatus

class CreateConversationRemoteOperation(
    private val title: String?,
    private val timestamp: Long
) : RemoteOperation<CreateConversation>() {
    @Suppress("TooGenericExceptionCaught")
    override fun run(client: NextcloudClient): RemoteOperationResult<CreateConversation> {
        val bodyMap =
            hashMapOf(
                "title" to title,
                "timestamp" to timestamp
            )

        val json = gson.toJson(bodyMap)
        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())

        val putMethod = PutMethod(client.baseUri.toString() + "$BASE_URL/new_session", true, requestBody)
        val status = putMethod.execute(client)

        return try {
            if (status == HttpStatus.SC_OK) {
                val responseBody = putMethod.getResponseBodyAsString()
                val type = object : TypeToken<CreateConversation>() {}.type
                val response: CreateConversation = gson.fromJson(responseBody, type)
                val result: RemoteOperationResult<CreateConversation> = RemoteOperationResult(true, putMethod)
                result.resultData = response
                result
            } else {
                RemoteOperationResult(false, putMethod)
            }
        } catch (e: Exception) {
            Log_OC.e(TAG, "create conversation: ", e)
            RemoteOperationResult(false, putMethod)
        } finally {
            putMethod.releaseConnection()
        }
    }

    companion object {
        private const val TAG = "CreateConversationRemoteOperation"
        private const val BASE_URL = "/ocs/v2.php/apps/assistant/chat"
    }
}
