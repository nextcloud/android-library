/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2015 ownCloud Inc.
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files

import at.bitfire.dav4jvm.exception.ConflictException
import at.bitfire.dav4jvm.exception.HttpException
import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.MkColMethod
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.internal.http.HTTP_BAD_METHOD

/**
 * Remote operation performing the creation of a new folder in the ownCloud server.
 *
 * @author David A. Velasco
 * @author masensio
 */
class CreateFolderRemoteOperation
/**
     * Constructor
     *
     * @param remotePath     full path to folder on the remote
     * @param createFullPath create higher level directories
     */
    @JvmOverloads
    constructor(
        private val remotePath: String,
        private val createFullPath: Boolean,
        private val token: String? = null
    ) : RemoteOperation<String?>() {
        /**
         * Performs the operation
         *
         * @param client Client object to communicate with the remote ownCloud server.
         */
        override fun run(client: NextcloudClient): RemoteOperationResult<String?> {
            return createFolder(client)
        }

        @Suppress("TooGenericExceptionCaught")
        private fun createFolder(client: NextcloudClient): RemoteOperationResult<String?> {
            var result: RemoteOperationResult<String?>
            try {
                val url: HttpUrl = client.getFilesDavUri(remotePath).toHttpUrl()
                val mkCol = MkColMethod(url)

                if (token?.isNotEmpty() == true) {
                    mkCol.addRequestHeader(E2E_TOKEN, token)
                }

                // will throw ConflictException if parent folder doesn't exist
                // will throw HttpException if folder already exists
                val response = client.execute(mkCol)

                result = RemoteOperationResult(response)

                val fileIdHeader = response.getHeader("OC-FileId")
                result.resultData = fileIdHeader

                Log_OC.d(TAG, "Create directory " + remotePath + ": " + result.logMessage)
            } catch (e: Exception) {
                if (e is ConflictException && remotePath != "/") {
                    // parent directory doesn't exist - try to create it recursively
                    // do not attempt to create root
                    result = createParentFolder(remotePath, client)

                    // check if parent directory/directories was/were created
                    // and create actual directory
                    if (result.isSuccess) {
                        result = createFolder(client)
                    }
                } else if (e is HttpException && e.code == HTTP_BAD_METHOD) {
                    // specified directory already exists
                    result = RemoteOperationResult(RemoteOperationResult.ResultCode.FOLDER_ALREADY_EXISTS)
                } else {
                    result = RemoteOperationResult(e)
                    Log_OC.e(TAG, "Create directory " + remotePath + ": " + result.logMessage, e)
                }
            }
            return result
        }

        private fun createParentFolder(
            filePath: String,
            nextcloudClient: NextcloudClient
        ): RemoteOperationResult<String?> {
            val parentPath = FileUtils.getParentPath(filePath)
            return CreateFolderRemoteOperation(parentPath, createFullPath).execute(nextcloudClient)
        }

        companion object {
            private val TAG = CreateFolderRemoteOperation::class.java.getSimpleName()
        }
    }
