/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2024 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.assistant.v1

import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.resources.status.NextcloudVersion
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test

class AssistantITV1 : AbstractIT() {
    @Before
    fun before() {
        testOnlyOnServer(NextcloudVersion.nextcloud_28)
    }

    @Test
    fun testGetTaskTypes() {
        val result = GetTaskTypesRemoteOperationV1().execute(nextcloudClient)
        assertTrue(result.isSuccess)

        val taskTypes = result.resultData.types
        assertTrue(taskTypes.isNotEmpty())
    }

    @Test
    fun testGetTaskList() {
        var result = GetTaskListRemoteOperationV1("assistant").execute(nextcloudClient)
        assertTrue(result.isSuccess)
        assertTrue(result.resultData.tasks.isEmpty())

        // create one task
        val input = "Give me some random output for test purpose"
        val type = "OCP\\TextProcessing\\FreePromptTaskType"
        assertTrue(CreateTaskRemoteOperationV1(input, type).execute(nextcloudClient).isSuccess)

        result = GetTaskListRemoteOperationV1("assistant").execute(nextcloudClient)
        assertTrue(result.isSuccess)

        val taskList = result.resultData.tasks
        assertTrue(taskList.isNotEmpty())
    }

    @Test
    fun testDeleteTask() {
        // create one task
        val input = "Give me some random output for test purpose"
        val type = "OCP\\TextProcessing\\FreePromptTaskType"
        assertTrue(CreateTaskRemoteOperationV1(input, type).execute(nextcloudClient).isSuccess)

        var result = GetTaskListRemoteOperationV1("assistant").execute(nextcloudClient)
        assertTrue(result.isSuccess)

        val tasks = result.resultData.tasks
        val countBefore = tasks.size

        // delete
        assertTrue(DeleteTaskRemoteOperationV1(tasks.first().id).execute(nextcloudClient).isSuccess)

        result = GetTaskListRemoteOperationV1("assistant").execute(nextcloudClient)
        assertTrue(result.isSuccess)

        assertEquals(countBefore - 1, result.resultData.tasks.size)
    }
}
