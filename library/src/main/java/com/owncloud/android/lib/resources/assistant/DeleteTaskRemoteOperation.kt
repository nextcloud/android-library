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

import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.DeleteMethod
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.resources.users.DeletePrivateKeyRemoteOperation
import java.io.IOException
import java.net.HttpURLConnection

class DeleteTaskRemoteOperation(private val appId: Long) : RemoteOperation<Void>() {
    override fun run(client: NextcloudClient): RemoteOperationResult<Void> {
        var postMethod: DeleteMethod? = null
        var result: RemoteOperationResult<Void>
        try {
            postMethod =
                DeleteMethod(
                    client.baseUri.toString() + DIRECT_ENDPOINT + appId,
                    true
                )
            val status = client.execute(postMethod)
            result = RemoteOperationResult<Void>(status == HttpURLConnection.HTTP_OK, postMethod)
        } catch (e: IOException) {
            result = RemoteOperationResult<Void>(e)
            Log_OC.e(TAG, "Deletion of task failed: " + result.logMessage, result.exception)
        } finally {
            postMethod?.releaseConnection()
        }
        return result
    }

    companion object {
        private val TAG = DeletePrivateKeyRemoteOperation::class.java.simpleName
        private const val DIRECT_ENDPOINT =
            "/ocs/v2.php/textprocessing/task/"
    }
}
