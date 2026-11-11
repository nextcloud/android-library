/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2024 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.assistant.v2.model

import com.owncloud.android.lib.resources.assistant.EmptyArrayAsObjectSerializer
import kotlinx.serialization.Serializable

@Serializable
data class TaskList(
    var tasks: List<Task>
)

@Serializable
data class Task(
    val id: Long,
    val type: String? = null,
    val status: String? = null,
    val userId: String? = null,
    val appId: String? = null,
    @Serializable(with = TaskInputSerializer::class)
    val input: TaskInput? = null,
    @Serializable(with = TaskOutputSerializer::class)
    val output: TaskOutput? = null,
    val completionExpectedAt: Int? = null,
    var progress: Double? = null,
    val lastUpdated: Int? = null,
    val scheduledAt: Int? = null,
    val endedAt: Int? = null
) {
    fun isTranslate(): Boolean = (type == TaskTypeData.TRANSLATE_TASK_TYPE_ID)
}

@Serializable
data class TaskInput(
    var input: String? = null
)

@Serializable
data class TaskOutput(
    var output: String? = null
)

internal object TaskInputSerializer : EmptyArrayAsObjectSerializer<TaskInput>(TaskInput.serializer())

internal object TaskOutputSerializer : EmptyArrayAsObjectSerializer<TaskOutput>(TaskOutput.serializer())
