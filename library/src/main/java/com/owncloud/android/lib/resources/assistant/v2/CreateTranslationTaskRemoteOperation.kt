/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.assistant.v2

import com.owncloud.android.lib.resources.assistant.v2.model.TaskTypeData
import com.owncloud.android.lib.resources.assistant.v2.model.TranslationRequest
import java.io.Serializable

class CreateTranslationTaskRemoteOperation(
    private val input: TranslationRequest,
    private val taskType: TaskTypeData
) : CreateTaskRemoteOperationV2(
        input = "",
        taskType = taskType
    ) {
    override fun buildRequestBody(): HashMap<String, Serializable?> =
        hashMapOf(
            "input" to input,
            "type" to taskType.id,
            "appId" to "assistant",
            "customId" to ""
        )
}
