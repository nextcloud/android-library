/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2023-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2023 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.tags

import com.nextcloud.common.JSONRequestBody
import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.PostMethod
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import org.apache.commons.httpclient.HttpStatus

class CreateTagRemoteOperation(val name: String) : RemoteOperation<Void>() {
    override fun run(client: NextcloudClient): RemoteOperationResult<Void> {
        val requestBody = JSONRequestBody("name", name)
        val postMethod = PostMethod(client.baseUri.toString() + TAG_URL, true, requestBody.get())

        val status = postMethod.execute(client)
        val isSuccess = status == HttpStatus.SC_CREATED

        return RemoteOperationResult(isSuccess, postMethod)
    }

    companion object {
        const val TAG_URL = "/remote.php/dav/systemtags/"
    }
}
