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
import com.owncloud.android.lib.resources.shares.ShareType
import org.apache.jackrabbit.webdav.property.DefaultDavProperty
import org.apache.jackrabbit.webdav.xml.Namespace
import org.json.JSONArray
import org.json.JSONObject

/**
 * Creates or removes the public share link of an album, depending on [isCreateShare].
 */
class PublicShareLinkAlbumRemoteOperation
    @JvmOverloads
    constructor(
        private val albumName: String,
        private val isCreateShare: Boolean,
        private val sessionTimeOut: SessionTimeOut = defaultSessionTimeOut
    ) : RemoteOperation<Any>() {
        @Deprecated("Deprecated in Java")
        override fun run(client: OwnCloudClient): RemoteOperationResult<Any> =
            client.setPhotosProperty(client.albumUri(albumName), collaboratorsProperty(), sessionTimeOut)

        /**
         * The server expects `[{"id":"","label":"Public link","type":3}]` to create a link and an empty
         * array to remove it again.
         */
        private fun collaboratorsProperty(): DefaultDavProperty<String> {
            val collaborators = JSONArray()
            if (isCreateShare) {
                collaborators.put(
                    JSONObject()
                        .put(WebdavEntry.SHAREES_ID, "")
                        .put(WebdavEntry.COLLABORATORS_SHARE_LABEL, PUBLIC_LINK_LABEL)
                        .put(WebdavEntry.SHAREES_SHARE_TYPE, ShareType.PUBLIC_LINK.value)
                )
            }

            return DefaultDavProperty(
                COLLABORATORS_PROPERTY,
                collaborators.toString(),
                Namespace.getNamespace(WebdavEntry.NAMESPACE_NC)
            )
        }

        companion object {
            private const val COLLABORATORS_PROPERTY = "nc:collaborators"
            private const val PUBLIC_LINK_LABEL = "Public Link"
        }
    }
