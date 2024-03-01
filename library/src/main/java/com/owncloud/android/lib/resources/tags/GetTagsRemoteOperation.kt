/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2023-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2023 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.tags

import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.network.WebdavEntry.Companion.EXTENDED_PROPERTY_NAME_REMOTE_ID
import com.owncloud.android.lib.common.network.WebdavEntry.Companion.NAMESPACE_OC
import com.owncloud.android.lib.common.network.WebdavEntry.Companion.SHAREES_DISPLAY_NAME
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import org.apache.commons.httpclient.HttpStatus
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet
import org.apache.jackrabbit.webdav.xml.Namespace

class GetTagsRemoteOperation : RemoteOperation<List<Tag>>() {
    @Deprecated("Deprecated in Java")
    override fun run(client: OwnCloudClient): RemoteOperationResult<List<Tag>> {
        val ocNamespace = Namespace.getNamespace(NAMESPACE_OC)

        val propSet =
            DavPropertyNameSet().apply {
                add(EXTENDED_PROPERTY_NAME_REMOTE_ID, ocNamespace)
                add(SHAREES_DISPLAY_NAME, ocNamespace)
            }

        val propFindMethod =
            PropFindMethod(
                client.baseUri.toString() + TAG_URL,
                propSet,
                1
            )

        val status = client.executeMethod(propFindMethod)

        return if (status == HttpStatus.SC_MULTI_STATUS) {
            val response = propFindMethod.responseBodyAsMultiStatus.responses

            val result = mutableListOf<Tag>()
            response.forEach {
                if (it.getProperties(HttpStatus.SC_OK).contentSize > 0) {
                    val id =
                        it.getProperties(HttpStatus.SC_OK)
                            .get(EXTENDED_PROPERTY_NAME_REMOTE_ID, ocNamespace).value as String
                    val name =
                        it.getProperties(HttpStatus.SC_OK)
                            .get(SHAREES_DISPLAY_NAME, ocNamespace).value as String

                    result.add(Tag(id, name))
                }
            }

            RemoteOperationResult<List<Tag>>(true, propFindMethod).apply {
                resultData = result
            }
        } else {
            RemoteOperationResult<List<Tag>>(false, propFindMethod).apply {
                resultData = emptyList()
            }
        }
    }

    companion object {
        const val TAG_URL = "/remote.php/dav/systemtags/"
    }
}
