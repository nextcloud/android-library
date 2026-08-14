/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-FileCopyrightText: 2026 TSI-mc <surinder.kumar@t-systems.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.albums

import com.nextcloud.common.SessionTimeOut
import com.nextcloud.common.defaultSessionTimeOut
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.network.WebdavEntry
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.operations.RemoteOperationResult.ResultCode
import com.owncloud.android.lib.resources.files.ToggleFavoriteRemoteOperation
import org.apache.jackrabbit.webdav.property.DefaultDavProperty
import org.apache.jackrabbit.webdav.xml.Namespace

class ToggleAlbumFavoriteRemoteOperation
    @JvmOverloads
    constructor(
        private val markAsFavorite: Boolean,
        private val filePath: String,
        private val sessionTimeOut: SessionTimeOut = defaultSessionTimeOut
    ) : RemoteOperation<Any>() {
        @Deprecated("Deprecated in Java")
        @Suppress("DEPRECATION")
        override fun run(client: OwnCloudClient): RemoteOperationResult<Any> {
            if (filePath.isBlank()) {
                return RemoteOperationResult(ResultCode.OK)
            }

            // a file taken from the local db carries its files path, not its album path, so it has to
            // go through the files endpoint instead
            return if (filePath.contains(ALBUM_PATH_SEGMENT)) {
                client.setPhotosProperty(client.photosUri(filePath), favoriteProperty(), sessionTimeOut)
            } else {
                ToggleFavoriteRemoteOperation(markAsFavorite, filePath).execute(client)
            }
        }

        private fun favoriteProperty(): DefaultDavProperty<String> =
            DefaultDavProperty(
                FAVORITE_PROPERTY,
                if (markAsFavorite) FAVORITE else NOT_FAVORITE,
                Namespace.getNamespace(WebdavEntry.NAMESPACE_OC)
            )

        companion object {
            private const val ALBUM_PATH_SEGMENT = "$ALBUMS_PATH/"
            private const val FAVORITE_PROPERTY = "oc:favorite"
            private const val FAVORITE = "1"
            private const val NOT_FAVORITE = "0"
        }
    }
