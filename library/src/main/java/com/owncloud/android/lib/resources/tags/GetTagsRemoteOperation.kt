/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2023 Tobias Kaminsky
 *   Copyright (C) 2023 Nextcloud GmbH
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 *   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 *   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
 *
 */

package com.owncloud.android.lib.resources.tags

import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.network.WebdavEntry.Companion.EXTENDED_PROPERTY_NAME_REMOTE_ID
import com.owncloud.android.lib.common.network.WebdavEntry.Companion.NAMESPACE_OC
import com.owncloud.android.lib.common.network.WebdavEntry.Companion.SHAREES_DISPLAY_NAME
import com.owncloud.android.lib.common.operations.LegacyRemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import org.apache.commons.httpclient.HttpStatus
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet
import org.apache.jackrabbit.webdav.xml.Namespace

class GetTagsRemoteOperation : LegacyRemoteOperation<List<Tag>>() {
    @Deprecated("Deprecated in Java")
    override fun run(client: OwnCloudClient): RemoteOperationResult<List<Tag>> {
        val ocNamespace = Namespace.getNamespace(NAMESPACE_OC)

        val propSet = DavPropertyNameSet().apply {
            add(EXTENDED_PROPERTY_NAME_REMOTE_ID, ocNamespace)
            add(SHAREES_DISPLAY_NAME, ocNamespace)
        }

        val propFindMethod = PropFindMethod(
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
                    val id = it.getProperties(HttpStatus.SC_OK)
                        .get(EXTENDED_PROPERTY_NAME_REMOTE_ID, ocNamespace).value as String
                    val name = it.getProperties(HttpStatus.SC_OK)
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
