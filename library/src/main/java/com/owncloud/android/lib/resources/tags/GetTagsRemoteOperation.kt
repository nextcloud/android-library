/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2023-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2024 ZetaTom <70907959+ZetaTom@users.noreply.github.com>
 * SPDX-FileCopyrightText: 2023 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.tags

import com.nextcloud.common.NextcloudClient
import com.owncloud.android.lib.common.network.ExtendedProperties
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import okhttp3.HttpUrl.Companion.toHttpUrl

class GetTagsRemoteOperation : RemoteOperation<List<Tag>>() {
    @Deprecated("Deprecated in Java")
    override fun run(client: NextcloudClient): RemoteOperationResult<List<Tag>> {
        val url = (client.baseUri.toString() + TAG_URL).toHttpUrl()

        val propertySet =
            arrayOf(
                ExtendedProperties.NAME_REMOTE_ID.toPropertyName(),
                ExtendedProperties.DISPLAY_NAME.toPropertyName()
            )

        val propFindMethod = com.nextcloud.operations.PropFindMethod(url, propertySet, 1)
        val propFindResult = client.execute(propFindMethod)
        val result = RemoteOperationResult<List<Tag>>(propFindResult.davResponse)

        val tags =
            propFindResult.children.mapNotNull { remoteFile ->
                remoteFile.remoteId?.let { remoteId ->
                    Tag(remoteId, remoteFile.name)
                }
            }

        result.resultData = tags

        return result
    }

    companion object {
        const val TAG_URL = "/remote.php/dav/systemtags/"
    }
}
