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

data class TaskTypes(
    val ocs: Ocs
)

data class TaskTypesData(
    val types: List<OcsType>
)

data class Ocs(
    val meta: OcsMeta,
    val data: TaskTypesData
)

data class OcsType(
    val id: String,
    val name: String,
    val description: String
)

data class OcsMeta(
    val status: String,
    val statuscode: Long,
    val message: String
)
