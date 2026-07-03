/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 TSI-mc <surinder.kumar@t-systems.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.albums

import com.nextcloud.common.SessionTimeOut
import com.nextcloud.common.defaultSessionTimeOut
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.network.WebdavEntry.Companion.COLLABORATORS_SHARE_LABEL
import com.owncloud.android.lib.common.network.WebdavEntry.Companion.NAMESPACE_NC
import com.owncloud.android.lib.common.network.WebdavEntry.Companion.SHAREES_ID
import com.owncloud.android.lib.common.network.WebdavEntry.Companion.SHAREES_SHARE_TYPE
import com.owncloud.android.lib.common.network.WebdavUtils
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.resources.shares.ShareType
import org.apache.commons.httpclient.HttpStatus
import org.apache.jackrabbit.webdav.client.methods.PropPatchMethod
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet
import org.apache.jackrabbit.webdav.property.DavPropertySet
import org.apache.jackrabbit.webdav.property.DefaultDavProperty
import org.apache.jackrabbit.webdav.xml.Namespace
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class PublicShareLinkAlbumRemoteOperation
@JvmOverloads
constructor(
    private val albumName: String,
    private val isCreateShare: Boolean,
    private val sessionTimeOut: SessionTimeOut = defaultSessionTimeOut
) : RemoteOperation<Any>() {
    @Deprecated("Deprecated in Java")
    override fun run(client: OwnCloudClient): RemoteOperationResult<Any> {
        var result: RemoteOperationResult<Any>
        var propPatchMethod: PropPatchMethod? = null

        val collaboratorsArray = JSONArray()

        // for create new share link the data should be in json
        // [{"id":"","label":"Public link","type":3}]
        // for removing share pass empty array []
        if (isCreateShare) {
            val collaboratorsObject = JSONObject()
            // empty while creating share
            collaboratorsObject.put(SHAREES_ID, "")
            // default public link
            collaboratorsObject.put(COLLABORATORS_SHARE_LABEL, "Public Link")
            collaboratorsObject.put(SHAREES_SHARE_TYPE, ShareType.PUBLIC_LINK.value)

            collaboratorsArray.put(collaboratorsObject)
        }

        val removeProperties = DavPropertyNameSet()

        val newProps = DavPropertySet()
        val collaboratorsProperty =
            DefaultDavProperty<Any>(
                "nc:collaborators",
                collaboratorsArray.toString(),
                Namespace.getNamespace(NAMESPACE_NC)
            )
        newProps.add(collaboratorsProperty)

        val fullUri = "${client.baseUri}/remote.php/dav/photos/${client.userId}/albums${
            WebdavUtils.encodePath(
                albumName
            )
        }"

        try {
            propPatchMethod = PropPatchMethod(fullUri, newProps, removeProperties)
            val status =
                client.executeMethod(
                    propPatchMethod,
                    sessionTimeOut.readTimeOut,
                    sessionTimeOut.connectionTimeOut
                )
            val isSuccess = (status == HttpStatus.SC_MULTI_STATUS || status == HttpStatus.SC_OK)
            if (isSuccess) {
                result = RemoteOperationResult<Any>(true, status, propPatchMethod.responseHeaders)
            } else {
                client.exhaustResponse(propPatchMethod.responseBodyAsStream)
                result = RemoteOperationResult<Any>(false, status, propPatchMethod.responseHeaders)
            }
        } catch (e: IOException) {
            result = RemoteOperationResult<Any>(e)
        } finally {
            propPatchMethod?.releaseConnection()
        }

        return result
    }
}
