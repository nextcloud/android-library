/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2024 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.assistant

import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.resources.assistant.model.TaskInputShape
import com.owncloud.android.lib.resources.assistant.model.TaskOutputShape
import com.owncloud.android.lib.resources.assistant.model.TaskTypeData
import com.owncloud.android.lib.resources.status.NextcloudVersion
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test

class AssistantIT : AbstractIT() {
    @Before
    fun before() {
        testOnlyOnServer(NextcloudVersion.nextcloud_30)
    }

    private fun getTaskType(): TaskTypeData {
        return TaskTypeData(
            "core:text2text",
            "Free text to text prompt",
            "Runs an arbitrary prompt through a language model that returns a reply",
            listOf(
                TaskInputShape(
                    "Prompt",
                    "Describe a task that you want the assistant to do or ask a question",
                    "Text"
                )
            ),
            listOf(
                TaskOutputShape(
                    "Generated reply",
                    "The generated text from the assistant",
                    "Text"
                )
            )
        )
    }

    @Test
    fun testGetTaskTypes() {
        val result = GetTaskTypesRemoteOperation().execute(nextcloudClient)
        assertTrue(result.isSuccess)

        val taskTypes = result.resultData
        assertTrue(taskTypes.isNotEmpty())
    }

    @Test
    fun testGetTaskList() {
        val selectedTaskType = "core:text2text"
        var result = GetTaskListRemoteOperation(selectedTaskType).execute(nextcloudClient)
        assertTrue(result.isSuccess)
        assertTrue(result.resultData.tasks.isEmpty())

        // create one task
        val input = "Give me some random output for test purpose"
        val taskType = getTaskType()
        assertTrue(CreateTaskRemoteOperation(input, taskType).execute(nextcloudClient).isSuccess)

        result = GetTaskListRemoteOperation(selectedTaskType).execute(nextcloudClient)
        assertTrue(result.isSuccess)

        val taskList = result.resultData.tasks
        assertTrue(taskList.isNotEmpty())
    }

    @Test
    fun testDeleteTask() {
        // create one task
        val input = "Give me some random output for test purpose"
        val taskType = getTaskType()
        val selectedTaskType = "core:text2text"
        assertTrue(CreateTaskRemoteOperation(input, taskType).execute(nextcloudClient).isSuccess)

        var result = GetTaskListRemoteOperation(selectedTaskType).execute(nextcloudClient)
        assertTrue(result.isSuccess)

        val tasks = result.resultData.tasks
        val countBefore = tasks.size

        // delete
        assertTrue(DeleteTaskRemoteOperation(tasks.first().id).execute(nextcloudClient).isSuccess)

        result = GetTaskListRemoteOperation(selectedTaskType).execute(nextcloudClient)
        assertTrue(result.isSuccess)

        assertEquals(countBefore - 1, result.resultData.tasks.size)
    }
}
