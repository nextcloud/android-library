/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2023-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2023 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.tags

import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.PutMethod
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.EMPTY_BYTE_ARRAY
import org.apache.commons.httpclient.HttpStatus

class PutTagRemoteOperation(
    val id: String,
    val fileId: Long
) : RemoteOperation<Void>() {
    override fun run(client: NextcloudClient): RemoteOperationResult<Void> {
        val empty: RequestBody = EMPTY_BYTE_ARRAY.toRequestBody()
        val putMethod =
            PutMethod(
                client.baseUri.toString() + TAG_URL + fileId + "/" + id,
                true,
                empty
            )

        val status = putMethod.execute(client)

        return if (status == HttpStatus.SC_CREATED) {
            RemoteOperationResult<Void>(true, putMethod)
        } else {
            RemoteOperationResult<Void>(false, putMethod)
        }
    }

    companion object {
        const val TAG_URL = "/remote.php/dav/systemtags-relations/files/"
    }
}
