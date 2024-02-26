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

import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.PostMethod
import com.nextcloud.operations.assistant.model.Ocs
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.apache.commons.httpclient.HttpStatus

class GetAssistantTaskTypes : RemoteOperation<Ocs>() {
    override fun run(client: NextcloudClient): RemoteOperationResult<Ocs> {
        val request = "{}".toRequestBody("application/json".toMediaTypeOrNull())
        val postMethod = PostMethod(client.baseUri.toString() + TAG_URL, true, request)
        val status = postMethod.execute(client)
        return RemoteOperationResult<Ocs>(status == HttpStatus.SC_CREATED, postMethod)
    }

    companion object {
        const val TAG_URL = "/ocs/v2.php/textprocessing/tasktype"
    }
}
