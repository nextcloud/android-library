/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2015 ownCloud Inc.
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files

import androidx.core.net.toUri
import at.bitfire.dav4jvm.DavCollection
import com.nextcloud.common.NextcloudClient
import com.nextcloud.common.SessionTimeOut
import com.nextcloud.common.defaultSessionTimeOut
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.network.WebdavEntry
import com.owncloud.android.lib.common.network.WebdavUtils
import com.owncloud.android.lib.common.network.WebdavUtils2
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.common.utils.WebDavFileUtils2
import com.owncloud.android.lib.resources.files.model.RemoteFile
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.apache.commons.httpclient.HttpStatus
import org.apache.jackrabbit.webdav.DavConstants
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod

/**
 * Remote operation performing the read a file from the ownCloud server.
 * 
 * @author David A. Velasco
 * @author masensio
 */
class ReadFileRemoteOperation
/**
 * Constructor
 * 
 * @param remotePath Remote path of the file.
 */ @JvmOverloads constructor(
    private val mRemotePath: String?,
    private val sessionTimeOut: SessionTimeOut = defaultSessionTimeOut
) : RemoteOperation<RemoteFile>() {
    /**
     * Performs the read operation.
     * 
     * @param client Client object to communicate with the remote ownCloud server.
     */
    override fun run(client: OwnCloudClient): RemoteOperationResult<RemoteFile> {
        var propfind: PropFindMethod? = null
        var result: RemoteOperationResult<RemoteFile>

        /** take the duty of check the server for the current state of the file there */
        try {
            // remote request
            propfind = PropFindMethod(
                client.getFilesDavUri(mRemotePath),
                WebdavUtils.getFilePropSet(),  // PropFind Properties
                DavConstants.DEPTH_0
            )
            val status: Int
            status = client.executeMethod(
                propfind,
                sessionTimeOut.readTimeOut,
                sessionTimeOut.connectionTimeOut
            )

            val isSuccess = (status == HttpStatus.SC_MULTI_STATUS ||
                status == HttpStatus.SC_OK
                )
            if (isSuccess) {
                // Parse response
                val resp = propfind.getResponseBodyAsMultiStatus()
                val we = WebdavEntry(
                    resp.getResponses()[0],
                    client.getFilesDavUri().getEncodedPath()!!
                )
                // Result of the operation
                result = RemoteOperationResult<RemoteFile>(true, propfind)
                result.resultData = RemoteFile(we)
            } else {
                result = RemoteOperationResult<RemoteFile>(false, propfind)
                client.exhaustResponse(propfind.getResponseBodyAsStream())
            }
        } catch (e: Exception) {
            result = RemoteOperationResult<RemoteFile>(e)
            Log_OC.e(
                TAG, "Read file " + mRemotePath + " failed: " + result.getLogMessage(),
                result.getException()
            )
        } finally {
            if (propfind != null) {
                propfind.releaseConnection()
            }
        }
        return result
    }

    override fun run(client: NextcloudClient): RemoteOperationResult<RemoteFile> {
        var result: RemoteFile? = null
        val location = client.getFilesDavUri(mRemotePath!!).toHttpUrl()

        val davCollection = DavCollection(client.disabledRedirectClient(), location)

        davCollection.propfind(depth = 1, *WebdavUtils2.PROPERTYSETS.ALL) { response, relation ->
            // This callback will be called for every file in the folder.
            // Use `response.properties` to access the successfully retrieved properties.
            if (response.isSuccess()) {
                result = WebDavFileUtils2.parseResponse(response, client.getFilesDavUri("/").toUri())
            }
        }

        return if (result == null) {
            RemoteOperationResult<RemoteFile>(RemoteOperationResult.ResultCode.UNKNOWN_ERROR)
        } else {
            RemoteOperationResult<RemoteFile>(RemoteOperationResult.ResultCode.OK).apply {
                resultData = result
            }
        }
    }

    companion object {
        private val TAG: String = ReadFileRemoteOperation::class.java.getSimpleName()
    }
}
