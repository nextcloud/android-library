/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.tags

import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.DeleteMethod
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import org.apache.commons.httpclient.HttpStatus

class DeleteTagRemoteOperation(
    val id: String,
    val fileId: Long
) : RemoteOperation<Void>() {
    override fun run(client: NextcloudClient): RemoteOperationResult<Void> {
        val deleteMethod =
            DeleteMethod(
                client.baseUri.toString() + TAG_URL + fileId + "/" + id,
                true
            )

        val status = deleteMethod.execute(client)

        return if (status == HttpStatus.SC_NO_CONTENT) {
            RemoteOperationResult<Void>(true, deleteMethod)
        } else {
            RemoteOperationResult<Void>(false, deleteMethod)
        }
    }

    companion object {
        const val TAG_URL = "/remote.php/dav/systemtags-relations/files/"
    }
}
