/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2024 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.assistant.v1.model

import kotlinx.serialization.Serializable

@Serializable
data class TaskList(
    var tasks: List<Task>
)

@Serializable
data class Task(
    val id: Long,
    val type: String? = null,
    val status: Long? = null,
    val userId: String? = null,
    val appId: String? = null,
    val input: String? = null,
    val output: String? = null,
    val identifier: String? = null,
    val completionExpectedAt: Long? = null
)

fun TaskList.toV2(): com.owncloud.android.lib.resources.assistant.v2.model.TaskList =
    com.owncloud.android.lib.resources.assistant.v2.model.TaskList(
        tasks =
            tasks.map { task ->
                com.owncloud.android.lib.resources.assistant.v2.model.Task(
                    id = task.id,
                    type = task.type,
                    status = task.status?.toString(),
                    userId = task.userId,
                    appId = task.appId,
                    input =
                        task.input?.let {
                            com.owncloud.android.lib.resources.assistant.v2.model
                                .TaskInput(input = it)
                        },
                    output =
                        task.output?.let {
                            com.owncloud.android.lib.resources.assistant.v2.model
                                .TaskOutput(output = it)
                        },
                    completionExpectedAt = task.completionExpectedAt?.toInt(),
                    progress = null,
                    lastUpdated = null,
                    scheduledAt = null,
                    endedAt = null
                )
            }
    )
