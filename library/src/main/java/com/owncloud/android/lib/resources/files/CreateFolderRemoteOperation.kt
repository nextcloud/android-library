/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Your Name <your@email.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files

import android.text.TextUtils
import at.bitfire.dav4jvm.DavCollection
import com.nextcloud.common.NextcloudClient
import com.nextcloud.common.SessionTimeOut
import com.nextcloud.common.defaultSessionTimeOut
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.apache.commons.httpclient.HttpStatus
import org.apache.jackrabbit.webdav.client.methods.MkColMethod

/**
 * Remote operation performing the creation of a new folder in the ownCloud server.
 *
 * @author David A. Velasco
 * @author masensio
 */
class CreateFolderRemoteOperation
    @JvmOverloads
    constructor(
        private val remotePath: String,
        private val createFullPath: Boolean,
        private val token: String?,
        private val sessionTimeOut: SessionTimeOut = defaultSessionTimeOut
    ) : RemoteOperation<String>() {
        /**
         * Constructor
         *
         * @param remotePath     Full path to the new directory to create in the remote server.
         * @param createFullPath 'True' means that all the ancestor folders should be created
         * if don't exist yet.
         */
        @JvmOverloads
        constructor(
            remotePath: String,
            createFullPath: Boolean,
            sessionTimeOut: SessionTimeOut = defaultSessionTimeOut
        ) : this(remotePath, createFullPath, "", sessionTimeOut)

        /**
         * Performs the operation
         *
         * @param client Client object to communicate with the remote ownCloud server.
         */
        override fun run(client: OwnCloudClient): RemoteOperationResult<String> {
            var result: RemoteOperationResult<String>

            result = createFolder(client)
            if (!result.isSuccess && createFullPath && RemoteOperationResult.ResultCode.CONFLICT == result.code &&
                ("/" != remotePath)
            ) { // this must already exists
                result = createParentFolder(FileUtils.getParentPath(remotePath), client)
                if (result.isSuccess) {
                    result = createFolder(client) // second (and last) try
                }
            }

            return result
        }

        override fun run(client: NextcloudClient): RemoteOperationResult<String> {
            var result: RemoteOperationResult<String>

            result = createFolder(client)
            if (!result.isSuccess && createFullPath && RemoteOperationResult.ResultCode.CONFLICT == result.code &&
                ("/" != remotePath)
            ) { // this must already exists
                result = createParentFolder(FileUtils.getParentPath(remotePath), client)
                if (result.isSuccess) {
                    result = createFolder(client) // second (and last) try
                }
            }

            return result
        }

        private fun createFolder(client: OwnCloudClient): RemoteOperationResult<String> {
            var result: RemoteOperationResult<String>
            var mkCol: MkColMethod? = null
            try {
                mkCol = MkColMethod(client.getFilesDavUri(remotePath))

                if (!TextUtils.isEmpty(token)) {
                    mkCol.addRequestHeader(E2E_TOKEN, token)
                }

                client.executeMethod(
                    mkCol,
                    sessionTimeOut.readTimeOut,
                    sessionTimeOut.connectionTimeOut
                )

                if (HttpStatus.SC_METHOD_NOT_ALLOWED == mkCol.statusCode) {
                    result = RemoteOperationResult<String>(RemoteOperationResult.ResultCode.FOLDER_ALREADY_EXISTS)
                } else {
                    result = RemoteOperationResult<String>(mkCol.succeeded(), mkCol)
                    val fileIdHeader = mkCol.getResponseHeader("OC-FileId")

                    if (fileIdHeader != null) {
                        val fileId = fileIdHeader.value

                        result.resultData = fileId
                    } else {
                        result.resultData = null
                    }
                }

                Log_OC.d(TAG, "Create directory " + remotePath + ": " + result.getLogMessage())
                client.exhaustResponse(mkCol.getResponseBodyAsStream())
            } catch (e: Exception) {
                result = RemoteOperationResult<String>(e)
                Log_OC.e(TAG, "Create directory " + remotePath + ": " + result.getLogMessage(), e)
            } finally {
                mkCol?.releaseConnection()
            }
            return result
        }

        private fun createFolder(client: NextcloudClient): RemoteOperationResult<String> {
            var result: RemoteOperationResult<String> =
                RemoteOperationResult(RemoteOperationResult.ResultCode.UNKNOWN_ERROR)
            try {
                val location = client.getFilesDavUri(remotePath).toHttpUrl()

                val c =
                    if (TextUtils.isEmpty(token)) {
                        client.disabledRedirectClient()
                    } else {
                        client
                            .disabledRedirectClient()
                            .newBuilder()
                            .addNetworkInterceptor { chain ->
                                chain.proceed(
                                    chain
                                        .request()
                                        .newBuilder()
                                        .header(E2E_TOKEN, token!!)
                                        .build()
                                )
                            }.build()
                    }

                val davCollection = DavCollection(c, location)

                davCollection.mkCol(null) { response ->
                    if (response.isSuccessful) {
                        result = RemoteOperationResult<String>(RemoteOperationResult.ResultCode.OK)
                        val fileIdHeader = response.header("OC-FileId", "")

                        if (fileIdHeader != "") {
                            result.resultData = fileIdHeader
                        } else {
                            result.resultData = null
                        }
                    } else if (response.code == HttpStatus.SC_METHOD_NOT_ALLOWED) {
                        result = RemoteOperationResult<String>(RemoteOperationResult.ResultCode.FOLDER_ALREADY_EXISTS)
                    } else {
                        result = RemoteOperationResult<String>(RemoteOperationResult.ResultCode.UNKNOWN_ERROR)
                    }

                    Log_OC.d(TAG, "Create directory " + remotePath + ": " + result.getLogMessage())
                }
            } catch (e: Exception) {
                result = RemoteOperationResult<String>(e)
                Log_OC.e(TAG, "Create directory " + remotePath + ": " + result.getLogMessage(), e)
            }

            return result
        }

        private fun createParentFolder(
            parentPath: String,
            client: OwnCloudClient
        ): RemoteOperationResult<String> {
            val operation: RemoteOperation<String> = CreateFolderRemoteOperation(parentPath, createFullPath)
            return operation.execute(client)
        }

        private fun createParentFolder(
            parentPath: String,
            client: NextcloudClient
        ): RemoteOperationResult<String> {
            val operation: RemoteOperation<String> = CreateFolderRemoteOperation(parentPath, createFullPath)
            return operation.execute(client)
        }

        companion object {
            private val TAG: String = CreateFolderRemoteOperation::class.java.getSimpleName()
        }
    }
