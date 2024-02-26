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

package com.nextcloud.operations.assistant

import android.content.Context
import com.nextcloud.common.User
import com.nextcloud.operations.assistant.model.CreatedTask
import com.nextcloud.operations.assistant.model.CreatedTaskData
import com.nextcloud.operations.assistant.model.TaskTypes
import com.owncloud.android.lib.common.network.NetworkManager

class AssistantRepository(user: User, context: Context) :
    AssistantRepositoryType {
    private val operation = NetworkManager(user, context)

    override fun getTaskTypes(): TaskTypes? {
        return operation.get("/ocs/v2.php/textprocessing/tasktypes", TaskTypes::class.java)
    }

    override fun createTask(
        input: String,
        type: String,
        appId: String,
        identifier: String,
    ): CreatedTask? {
        val json = """
        {
            "input": $input,
            "type": $type,
            "appId": $appId,
            "identifier": $identifier
        }
    """.trimIndent()

        return operation.post(
            "/ocs/v2.php/textprocessing/schedule",
            CreatedTaskData::class.java,
            json
        )
    }
}
