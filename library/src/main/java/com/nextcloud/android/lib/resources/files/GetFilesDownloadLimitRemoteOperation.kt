/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2024 ZetaTom <70907959+zetatom@users.noreply.github.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.files

import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.network.WebdavEntry
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.resources.OCSRemoteOperation
import org.apache.commons.httpclient.HttpStatus
import org.apache.jackrabbit.webdav.DavConstants
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet
import org.apache.jackrabbit.webdav.xml.Namespace

class GetFilesDownloadLimitRemoteOperation
    @JvmOverloads
    constructor(
        val remotePath: String,
        val subfiles: Boolean = false
    ) : OCSRemoteOperation<List<FileDownloadLimit>>() {
        @Deprecated("Deprecated in Java")
        override fun run(client: OwnCloudClient): RemoteOperationResult<List<FileDownloadLimit>> {
            var result: RemoteOperationResult<List<FileDownloadLimit>>
            var propFindMethod: PropFindMethod? = null
            val propSet = DavPropertyNameSet()
            val depth = if (subfiles) DavConstants.DEPTH_1 else DavConstants.DEPTH_0

            propSet.add(
                WebdavEntry.EXTENDED_PROPERTY_FILE_DOWNLOAD_LIMITS,
                Namespace.getNamespace(WebdavEntry.NAMESPACE_NC)
            )

            try {
                propFindMethod = PropFindMethod(client.getFilesDavUri(remotePath), propSet, depth)

                val status = client.executeMethod(propFindMethod)

                if (status == HttpStatus.SC_MULTI_STATUS || status == HttpStatus.SC_OK) {
                    val response = propFindMethod.responseBodyAsMultiStatus

                    val fileDownloadLimits =
                        response.responses.flatMap {
                            val webdavEntry = WebdavEntry(it, client.filesDavUri.encodedPath!!)
                            webdavEntry.fileDownloadLimit
                        }

                    result = RemoteOperationResult(true, propFindMethod)
                    result.resultData = fileDownloadLimits
                } else {
                    result = RemoteOperationResult(false, propFindMethod)
                    client.exhaustResponse(propFindMethod.responseBodyAsStream)
                }
            } catch (e: Exception) {
                result = RemoteOperationResult(e)
                Log_OC.e(TAG, "Exception while reading download limit", e)
            } finally {
                propFindMethod?.releaseConnection()
            }

            return result
        }

        companion object {
            private val TAG = GetFilesDownloadLimitRemoteOperation::class.java.simpleName
        }
    }
