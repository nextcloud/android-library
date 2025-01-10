/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2024 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.assistant.v2.model

data class TaskTypes(
    val types: Map<String, TaskTypeData>
)

data class TaskTypeData(
    val id: String?,
    val name: String?,
    val description: String?,
    val inputShape: TaskInputShape?,
    val outputShape: TaskOutputShape?
)

data class TaskInputShape(
    val input: Shape?
)

data class TaskOutputShape(
    val output: Shape?
)

data class Shape(
    val name: String,
    val description: String,
    val type: String
)
