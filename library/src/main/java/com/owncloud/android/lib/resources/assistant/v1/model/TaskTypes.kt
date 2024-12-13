/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2024 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.assistant.v1.model

import com.owncloud.android.lib.resources.assistant.v2.model.TaskTypeData

data class TaskTypes(
    var types: List<TaskType>
)

data class TaskType(
    val id: String?,
    val name: String?,
    val description: String?
)

fun TaskTypes.toV2(): List<TaskTypeData> {
    return types.map { taskType ->
        TaskTypeData(
            id = taskType.id,
            name = taskType.name,
            description = taskType.description,
            inputShape = null,
            outputShape = null
        )
    }
}
