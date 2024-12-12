/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2024 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.assistant.model

data class TaskTypes(val types: Map<String, TaskTypeData>)

data class TaskTypeData(
    val id: String?,
    val name: String?,
    val description: String?,
    val inputShape: List<TaskInputShape>?,
    val outputShape: List<TaskOutputShape>?
)

data class TaskInputShape(
    val name: String?,
    val description: String?,
    val type: String?
)

data class TaskOutputShape(
    val name: String?,
    val description: String?,
    val type: String?
)
