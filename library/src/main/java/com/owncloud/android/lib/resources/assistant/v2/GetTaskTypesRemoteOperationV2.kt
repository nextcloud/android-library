/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2024 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.assistant.v2

import com.google.gson.reflect.TypeToken
import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.GetMethod
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.ocs.ServerResponse
import com.owncloud.android.lib.resources.OCSRemoteOperation
import com.owncloud.android.lib.resources.assistant.v2.model.TaskTypeData
import com.owncloud.android.lib.resources.assistant.v2.model.TaskTypes
import org.apache.commons.httpclient.HttpStatus

/**
 * Returns a list of supported task types.
 *
 * Example JSON representation of one task type:
 * ```
 * {
 *   "id": "core:text2text",
 *   "name": "Free text to text prompt",
 *   "description": "Runs an arbitrary prompt through a language model that returns a reply",
 *   "inputShape": {
 *     "input": {
 *       "name": "Prompt",
 *       "description": "Describe a task that you want the assistant to do or ask a question",
 *       "type": "Text"
 *     }
 *   },
 *   "outputShape": {
 *     "output": {
 *       "name": "Generated reply",
 *       "description": "The generated text from the assistant",
 *       "type": "Text"
 *     }
 *   }
 * }
 * ```
 */
class GetTaskTypesRemoteOperationV2 : OCSRemoteOperation<List<TaskTypeData>>() {
    private val supportedTaskType = "Text"
    private val chatTaskName = "Chat"

    @Suppress("TooGenericExceptionCaught")
    override fun run(client: NextcloudClient): RemoteOperationResult<List<TaskTypeData>> {
        var result: RemoteOperationResult<List<TaskTypeData>>
        var getMethod: GetMethod? = null

        try {
            getMethod =
                GetMethod(client.baseUri.toString() + DIRECT_ENDPOINT + JSON_FORMAT, true)
            val status = client.execute(getMethod)
            if (status == HttpStatus.SC_OK) {
                val response =
                    getServerResponse(
                        getMethod,
                        object : TypeToken<ServerResponse<TaskTypes>>() {}
                    )

                val taskTypeList =
                    response
                        ?.ocs
                        ?.data
                        ?.types
                        ?.map { (key, value) -> value.copy(id = value.id ?: key) }
                        ?.filter { taskType ->
                            isSingleTextInputOutput(taskType) || taskType.name == chatTaskName
                        }
                        ?.sortedByDescending { it.name == chatTaskName }

                result = RemoteOperationResult(true, getMethod)
                result.resultData = taskTypeList
            } else {
                result = RemoteOperationResult(false, getMethod)
            }
        } catch (e: Exception) {
            result = RemoteOperationResult(e)
            Log_OC.e(
                TAG,
                "Get task types for user " + " failed: " + result.logMessage,
                result.exception
            )
        } finally {
            getMethod?.releaseConnection()
        }
        return result
    }

    private fun isSingleTextInputOutput(taskType: TaskTypeData): Boolean {
        val inputShape = taskType.inputShape
        val outputShape = taskType.outputShape

        val hasOneTextInput =
            inputShape.size == 1 &&
                inputShape.values.first().type == supportedTaskType

        val hasOneTextOutput =
            outputShape.size == 1 &&
                outputShape.values.first().type == supportedTaskType

        return hasOneTextInput && hasOneTextOutput
    }

    companion object {
        private val TAG = GetTaskTypesRemoteOperationV2::class.java.simpleName
        private const val DIRECT_ENDPOINT = "/ocs/v2.php/taskprocessing/tasktypes"
    }
}
