/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2024 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.assistant.model

import com.google.gson.annotations.SerializedName

private const val generateTextId = "core:text2text"
private const val extractTopicsId = "core:text2text:topics"
private const val generateHeadlineId = "core:text2text:headline"
private const val summarizeId = "core:text2text:summary"

data class TaskTypes(val types: TaskType)

data class TaskType(
    @SerializedName(generateTextId)
    val generateText: TaskTypeData,
    @SerializedName(extractTopicsId)
    val extractTopics: TaskTypeData,
    @SerializedName(generateHeadlineId)
    val generateHeadline: TaskTypeData,
    @SerializedName(summarizeId)
    val summarize: TaskTypeData
)

data class TaskTypeData(val id: String?, val name: String?, val description: String?)

fun TaskTypes.toTaskTypeDataList(): List<TaskTypeData> {
    return listOf(
        types.generateText to generateTextId,
        types.extractTopics to extractTopicsId,
        types.generateHeadline to generateHeadlineId,
        types.summarize to summarizeId
    ).map { (taskData, id) -> taskData.copy(id = taskData.id ?: id) }
}
