/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files

import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.GetMethod
import com.owncloud.android.lib.common.network.OnDatatransferProgressListener
import com.owncloud.android.lib.common.network.WebdavUtils
import com.owncloud.android.lib.common.operations.OperationCancelledException
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import org.apache.commons.httpclient.HttpStatus
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

class DownloadFileRemoteOperation(private val remotePath: String, private val temporalFolderPath: String?) :
    RemoteOperation<Any?>() {
    private val dataTransferListeners: MutableSet<OnDatatransferProgressListener?> =
        HashSet()
    private val cancellationRequested = AtomicBoolean(false)
    var modificationTimestamp: Long = 0
        private set
    var etag: String = ""
        private set

    override fun run(client: NextcloudClient): RemoteOperationResult<Any> {
        var result: RemoteOperationResult<Any>
        val tmpFile = File(this.tmpPath)

        try {
            val parentFile =
                tmpFile.getParentFile() ?: return RemoteOperationResult<Any>(Exception("parent file is null"))

            val isTempFileCreated = parentFile.mkdirs()
            if (isTempFileCreated) {
                Log_OC.d(TAG, "temp file created")
            }

            val getMethod = GetMethod(client.getFilesDavUri(remotePath), false)
            val status = downloadFile(getMethod, client, tmpFile)
            result = RemoteOperationResult<Any>(isSuccess(status), getMethod)
            Log_OC.i(
                TAG, "Download of " + remotePath + " to " + this.tmpPath + ": " +
                    result.getLogMessage()
            )
        } catch (e: Exception) {
            result = RemoteOperationResult<Any>(e)
            Log_OC.e(
                TAG, "Download of " + remotePath + " to " + this.tmpPath + ": " +
                    result.getLogMessage(), e
            )
        }

        return result
    }

    @Throws(IOException::class, OperationCancelledException::class, CreateLocalFileException::class)
    private fun downloadFile(getMethod: GetMethod, client: NextcloudClient, targetFile: File): Int {
        var status: Int
        var it: MutableIterator<OnDatatransferProgressListener?>?

        var fos: FileOutputStream? = null
        try {
            status = client.execute(getMethod)

            if (isSuccess(status)) {
                try {
                    val isTargetFileCreated = targetFile.createNewFile()
                    if (isTargetFileCreated) {
                        Log_OC.i(TAG, "target file is created")
                    }
                } catch (ex: IOException) {
                    Log_OC.e(TAG, "Error creating file " + targetFile.absolutePath, ex)
                    throw CreateLocalFileException(targetFile.path, ex)
                } catch (ex: SecurityException) {
                    Log_OC.e(TAG, "Error creating file " + targetFile.absolutePath, ex)
                    throw CreateLocalFileException(targetFile.path, ex)
                }

                val bis = BufferedInputStream(getMethod?.getResponseBodyAsStream())
                fos = FileOutputStream(targetFile)

                val bytes = ByteArray(4096)
                var readResult: Int
                while ((bis.read(bytes).also { readResult = it }) != -1) {
                    synchronized(cancellationRequested) {
                        if (cancellationRequested.get()) {
                            throw OperationCancelledException()
                        }
                    }

                    fos.write(bytes, 0, readResult)

                    synchronized(dataTransferListeners) {
                        it = dataTransferListeners.iterator()
                    }
                }

                var modificationTime = getMethod.getResponseHeader("Last-Modified")
                if (modificationTime == null) {
                    modificationTime = getMethod.getResponseHeader("last-modified")
                }

                if (modificationTime != null) {
                    val d = WebdavUtils.parseResponseDate(modificationTime)
                    modificationTimestamp = d?.time ?: 0
                } else {
                    Log_OC.e(
                        TAG,
                        "Could not read modification time from response downloading $remotePath"
                    )
                }

                this.etag = WebdavUtils.getEtagFromResponse(getMethod)
                if (etag.isEmpty()) {
                    Log_OC.e(TAG, "Could not read eTag from response downloading $remotePath")
                }
            }
        } finally {
            fos?.close()

            // let the connection available for other methods
            getMethod.releaseConnection()
        }
        return status
    }

    private fun isSuccess(status: Int): Boolean {
        return (status == HttpStatus.SC_OK)
    }

    private val tmpPath: String
        get() = temporalFolderPath + remotePath

    fun addDatatransferProgressListener(listener: OnDatatransferProgressListener?) {
        synchronized(dataTransferListeners) {
            dataTransferListeners.add(listener)
        }
    }

    fun removeDatatransferProgressListener(listener: OnDatatransferProgressListener?) {
        synchronized(dataTransferListeners) {
            dataTransferListeners.remove(listener)
        }
    }

    fun cancel() {
        cancellationRequested.set(true)
    }

    companion object {
        private val TAG: String = DownloadFileRemoteOperation::class.java.getSimpleName()
    }
}
