/*
 * Nextcloud Android client application
 *
 * @author Alper Ozturk
 * Copyright (C) 2024 Alper Ozturk
 * Copyright (C) 2024 Nextcloud GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.nextcloud.operations.assistant.model

data class CreatedTask (
    val ocs: CreatedTaskOcs
)

data class CreatedTaskOcs (
    val meta: Meta,
    val data: CreatedTaskData
)

data class CreatedTaskData (
    val task: Task
)

data class Task (
    val id: Long,
    val type: String,
    val status: Long,
    val userId: String? = null,
    val appId: String,
    val input: String,
    val output: String? = null,
    val identifier: String,
    val completionExpectedAt: String? = null
)

data class Meta (
    val status: String,
    val statuscode: Long,
    val message: String
)
