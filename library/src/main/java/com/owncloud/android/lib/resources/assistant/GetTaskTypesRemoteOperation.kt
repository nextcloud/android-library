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

package com.owncloud.android.lib.resources.assistant

import com.google.gson.reflect.TypeToken
import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.GetMethod
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.ocs.ServerResponse
import com.owncloud.android.lib.resources.OCSRemoteOperation
import com.owncloud.android.lib.resources.assistant.model.TaskTypes
import org.apache.commons.httpclient.HttpStatus

class GetTaskTypesRemoteOperation : OCSRemoteOperation<TaskTypes>() {
    @Suppress("TooGenericExceptionCaught")
    override fun run(client: NextcloudClient): RemoteOperationResult<TaskTypes> {
        var result: RemoteOperationResult<TaskTypes>
        var getMethod: GetMethod? = null
        try {
            getMethod =
                GetMethod(client.baseUri.toString() + DIRECT_ENDPOINT + JSON_FORMAT, true)
            val status = client.execute(getMethod)
            if (status == HttpStatus.SC_OK) {
                val taskTypes: TaskTypes =
                    getServerResponse(
                        getMethod,
                        object : TypeToken<ServerResponse<TaskTypes>>() {}
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
        private const val DIRECT_ENDPOINT = "/ocs/v2.php/textprocessing/tasktypes"
    }
}
