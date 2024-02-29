/*
 * Nextcloud Android Library is available under MIT license
 *
 * @author Alper Ozturk
 * Copyright (C) 2024 Alper Ozturk
 * Copyright (C) 2024 Nextcloud GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.owncloud.android.lib.resources.assistant

import com.google.gson.reflect.TypeToken
import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.GetMethod
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.ocs.ServerResponse
import com.owncloud.android.lib.resources.OCSRemoteOperation
import com.owncloud.android.lib.resources.assistant.model.TaskList
import org.apache.commons.httpclient.HttpStatus

class GetTaskListRemoteOperation(private val appId: String) : OCSRemoteOperation<TaskList>() {
    @Suppress("TooGenericExceptionCaught")
    override fun run(client: NextcloudClient): RemoteOperationResult<TaskList> {
        var result: RemoteOperationResult<TaskList>
        var getMethod: GetMethod? = null
        try {
            getMethod =
                GetMethod(client.baseUri.toString() + DIRECT_ENDPOINT + appId + JSON_FORMAT, true)
            val status = client.execute(getMethod)
            if (status == HttpStatus.SC_OK) {
                val taskTypes: TaskList =
                    getServerResponse(
                        getMethod,
                        object : TypeToken<ServerResponse<TaskList>>() {}
                    )
                        .ocs.data
                result = RemoteOperationResult(true, getMethod)
                result.setResultData(taskTypes)
            } else {
                result = RemoteOperationResult(false, getMethod)
            }
        } catch (e: Exception) {
            result = RemoteOperationResult(e)
            Log_OC.e(
                TAG,
                "Get task list for user " + " failed: " + result.logMessage,
                result.exception
            )
        } finally {
            getMethod?.releaseConnection()
        }
        return result
    }

    companion object {
        private val TAG = GetTaskTypesRemoteOperation::class.java.simpleName
        private const val DIRECT_ENDPOINT = "/ocs/v2.php/textprocessing/tasks/app/"
    }
}
