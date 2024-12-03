/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2024 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.assistant.model

data class TaskList(
    var tasks: List<Task>
)

data class Task(
    val id: Long,
    val type: String?,
    val status: String?,
    val userId: String?,
    val appId: String?,
    val input: TaskInput?,
    val output: TaskOutput?,
    val completionExpectedAt: Int? = null,
    var progress: Int? = null,
    val lastUpdated: Int? = null,
    val scheduledAt: Int? = null,
    val endedAt: Int? = null
)

data class TaskInput (
    var input: String? = null
)

data class TaskOutput (
   var output : String? = null
)
