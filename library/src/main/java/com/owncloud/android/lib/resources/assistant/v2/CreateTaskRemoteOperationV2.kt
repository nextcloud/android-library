/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2024 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.assistant.v2

import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.PostMethod
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.resources.assistant.v2.model.TaskTypeData
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.apache.commons.httpclient.HttpStatus

open class CreateTaskRemoteOperationV2(
    private val input: String,
    private val taskType: TaskTypeData
) : RemoteOperation<Void>() {
    protected open fun buildRequestBody(): String {
        val inputField = hashMapOf("input" to input)

        val jsonObject =
            buildJsonObject {
                put("input", Json.encodeToJsonElement(inputField))
                put("type", taskType.id)
                put("appId", "assistant")
                put("customId", "")
            }

        return Json.encodeToString(jsonObject)
    }

    override fun run(client: NextcloudClient): RemoteOperationResult<Void> {
        val json = buildRequestBody()
        val request = json.toRequestBody("application/json".toMediaTypeOrNull())
        val postMethod = PostMethod(client.baseUri.toString() + TAG_URL, true, request)
        val status = postMethod.execute(client)

        return if (status == HttpStatus.SC_OK) {
            RemoteOperationResult(true, postMethod)
        } else {
            RemoteOperationResult(false, postMethod)
        }
    }

    companion object {
        const val TAG_URL = "/ocs/v2.php/taskprocessing/schedule"
    }
}
