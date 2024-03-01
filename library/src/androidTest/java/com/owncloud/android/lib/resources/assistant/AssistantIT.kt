/*
 * Nextcloud Android client application
 *
 * @author Tobias Kaminsky
 * Copyright (C) 2024 Tobias Kaminsky
 * Copyright (C) 2024 Nextcloud GmbH
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *  
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 *  
 */

package com.owncloud.android.lib.resources.assistant

import com.owncloud.android.AbstractIT
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

class AssistantIT : AbstractIT() {
    @Test
    fun testGetTaskTypes() {
        val result = GetTaskTypesRemoteOperation().execute(nextcloudClient)
        assertTrue(result.isSuccess)

        val taskTypes = result.resultData.types
        assertTrue(taskTypes.isNotEmpty())
    }

    @Test
    fun testGetTaskList() {
        var result = GetTaskListRemoteOperation("assistant").execute(nextcloudClient)
        assertTrue(result.isSuccess)
        assertTrue(result.resultData.tasks.isEmpty())

        // create one task
        val input = "Give me some random output for test purpose"
        val type = "OCP\\TextProcessing\\FreePromptTaskType"
        assertTrue(CreateTaskRemoteOperation(input, type).execute(nextcloudClient).isSuccess)

        result = GetTaskListRemoteOperation("assistant").execute(nextcloudClient)
        assertTrue(result.isSuccess)

        val taskList = result.resultData.tasks
        assertTrue(taskList.isNotEmpty())
    }

    @Test
    fun testDeleteTask() {
        // create one task
        val input = "Give me some random output for test purpose"
        val type = "OCP\\TextProcessing\\FreePromptTaskType"
        assertTrue(CreateTaskRemoteOperation(input, type).execute(nextcloudClient).isSuccess)

        var result = GetTaskListRemoteOperation("assistant").execute(nextcloudClient)
        assertTrue(result.isSuccess)

        val tasks = result.resultData.tasks
        val countBefore = tasks.size

        // delete
        assertTrue(DeleteTaskRemoteOperation(tasks.first().id).execute(nextcloudClient).isSuccess)

        result = GetTaskListRemoteOperation("assistant").execute(nextcloudClient)
        assertTrue(result.isSuccess)

        assertEquals(countBefore - 1, result.resultData.tasks.size)
    }
}
