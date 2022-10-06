/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2018-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2024 ZetaTom <70907959+ZetaTom@users.noreply.github.com>
 * SPDX-FileCopyrightText: 2018 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files

import com.nextcloud.common.NextcloudClient
import com.owncloud.android.lib.common.network.ExtendedProperties
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import okhttp3.HttpUrl.Companion.toHttpUrl

/**
 * Links live photos
 */
class LinkLivePhotoRemoteOperation(
    private val path: String,
    private val linkedFileName: String
) : RemoteOperation<Void>() {
    override fun run(client: NextcloudClient): RemoteOperationResult<Void> {
        val url = client.getFilesDavUri(path).toHttpUrl()
        val metadataLiveProperty = mapOf(Pair(ExtendedProperties.METADATA_LIVE_PHOTO.toPropertyName(), linkedFileName))
        val propPatchMethod = com.nextcloud.operations.PropPatchMethod(url, setProperties = metadataLiveProperty)
        val response = client.execute(propPatchMethod)

        return RemoteOperationResult(response)
    }
}
