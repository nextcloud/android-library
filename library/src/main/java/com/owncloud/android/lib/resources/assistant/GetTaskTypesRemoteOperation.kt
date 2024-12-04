/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2024 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.assistant

import com.google.gson.reflect.TypeToken
import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.GetMethod
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.ocs.ServerResponse
import com.owncloud.android.lib.resources.OCSRemoteOperation
import com.owncloud.android.lib.resources.assistant.model.TaskTypeData
import com.owncloud.android.lib.resources.assistant.model.TaskTypes
import org.apache.commons.httpclient.HttpStatus

class GetTaskTypesRemoteOperation : OCSRemoteOperation<List<TaskTypeData>>() {

    private val supportedTaskType = "Text"

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
                    )?.ocs?.data?.types

                val taskTypeList = response?.map { (key, value) ->
                    value.copy(id = value.id ?: key)
                }

                val supportedTaskTypeList = taskTypeList?.filter { taskType ->
                    taskType.inputShape?.any { inputShape ->
                        inputShape.type == supportedTaskType
                    } == true && taskType.outputShape?.any { outputShape ->
                        outputShape.type == supportedTaskType
                    } == true
                }

                result = RemoteOperationResult(true, getMethod)
                result.setResultData(supportedTaskTypeList)
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

    companion object {
        private val TAG = GetTaskTypesRemoteOperation::class.java.simpleName
        private const val DIRECT_ENDPOINT = "/ocs/v2.php/taskprocessing/tasktypes"
    }
}
