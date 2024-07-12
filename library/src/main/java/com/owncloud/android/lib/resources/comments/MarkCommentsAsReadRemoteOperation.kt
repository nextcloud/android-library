/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2018-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2024 ZetaTom <70907959+ZetaTom@users.noreply.github.com>
 * SPDX-FileCopyrightText: 2018 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.comments

import com.nextcloud.common.NextcloudClient
import com.owncloud.android.lib.common.network.ExtendedProperties
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import okhttp3.HttpUrl.Companion.toHttpUrl

/**
 * Mark all comments for a file as read
 */
class MarkCommentsAsReadRemoteOperation(private val fileId: Long) : RemoteOperation<Void>() {
    override fun run(client: NextcloudClient): RemoteOperationResult<Void> {
        val url = client.getCommentsUri(fileId).toHttpUrl()
        val readMarkerProperty = mapOf(Pair(ExtendedProperties.COMMENTS_READ_MARKER.toPropertyName(), ""))
        val propPatchMethod = com.nextcloud.operations.PropPatchMethod(url, setProperties = readMarkerProperty)
        val response = client.execute(propPatchMethod)

        return RemoteOperationResult(response)
    }
}
