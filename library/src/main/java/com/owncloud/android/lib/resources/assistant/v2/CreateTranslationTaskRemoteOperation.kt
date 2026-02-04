/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.assistant.v2

import com.owncloud.android.lib.resources.assistant.v2.model.TaskTypeData
import com.owncloud.android.lib.resources.assistant.v2.model.TranslationRequest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put

class CreateTranslationTaskRemoteOperation(
    private val input: TranslationRequest,
    private val taskType: TaskTypeData
) : CreateTaskRemoteOperationV2(
        input = "",
        taskType = taskType
    ) {
    override fun buildRequestBody(): String {
        val jsonObject =
            buildJsonObject {
                put("input", Json.encodeToJsonElement(input))
                put("type", taskType.id)
                put("appId", "assistant")
                put("customId", "")
            }

        return Json.encodeToString(jsonObject)
    }
}
