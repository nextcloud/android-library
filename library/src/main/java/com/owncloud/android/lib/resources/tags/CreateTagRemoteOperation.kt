/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2023-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2023 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.tags

import com.google.gson.Gson
import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.PostMethod
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.apache.commons.httpclient.HttpStatus

class CreateTagRemoteOperation(val name: String) : RemoteOperation<Void>() {
    override fun run(client: NextcloudClient): RemoteOperationResult<Void> {
        val map = HashMap<String, String>()
        map["name"] = name

        val json = Gson().toJson(map)

        val request = RequestBody.create("application/json".toMediaTypeOrNull(), json)

        val postMethod = PostMethod(client.baseUri.toString() + TAG_URL, true, request)

        val status = postMethod.execute(client)

        return if (status == HttpStatus.SC_CREATED) {
            RemoteOperationResult<Void>(true, postMethod)
        } else {
            RemoteOperationResult<Void>(false, postMethod)
        }
    }

    companion object {
        const val TAG_URL = "/remote.php/dav/systemtags/"
    }
}
