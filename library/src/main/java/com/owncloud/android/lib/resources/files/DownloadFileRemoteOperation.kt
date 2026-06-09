/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files

import android.os.Build
import androidx.annotation.RequiresApi
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
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicBoolean

@Suppress("NestedBlockDepth", "TooGenericExceptionCaught", "ThrowsCount")
@RequiresApi(Build.VERSION_CODES.O)
class DownloadFileRemoteOperation(
    private val remotePath: String,
    private val temporalFolderPath: String?
) : RemoteOperation<Any>() {
    private val dataTransferListeners: MutableSet<OnDatatransferProgressListener?> = HashSet()
    private val cancellationRequested = AtomicBoolean(false)
    var modificationTimestamp: Long = 0
        private set
    var etag: String = ""
        private set

    @Suppress("DEPRECATION")
    override fun run(client: NextcloudClient): RemoteOperationResult<Any> {
        val targetPath = Paths.get(tmpPath)
        return try {
            val parent = targetPath.parent ?: throw IOException("No parent directory for: $targetPath")
            Files.createDirectories(parent)
            val getMethod = GetMethod(client.getFilesDavUri(remotePath), false)
            val status = downloadFile(getMethod, client, targetPath)
            RemoteOperationResult<Any>(isSuccess(status), getMethod).also {
                Log_OC.i(TAG, "Download of $remotePath to $targetPath: ${it.logMessage}")
            }
        } catch (e: Exception) {
            RemoteOperationResult<Any>(e).also {
                Log_OC.e(TAG, "Download of $remotePath to $targetPath: ${it.logMessage}", e)
            }
        }
    }

    // region private methods
    @Throws(IOException::class, OperationCancelledException::class, CreateLocalFileException::class)
    private fun downloadFile(
        getMethod: GetMethod,
        client: NextcloudClient,
        targetPath: Path
    ): Int {
        val status = client.execute(getMethod)
        if (!isSuccess(status)) {
            getMethod.releaseConnection()
            return status
        }

        try {
            writeResponseToFile(getMethod, targetPath)
            readMetadata(getMethod)
        } finally {
            getMethod.releaseConnection()
        }

        return status
    }

    private fun writeResponseToFile(
        getMethod: GetMethod,
        targetPath: Path
    ) {
        val responseStream =
            getMethod.getResponseBodyAsStream()
                ?: throw IOException("Empty response body for $remotePath")

        val outputStream =
            try {
                Files.newOutputStream(targetPath)
            } catch (ex: IOException) {
                Log_OC.e(TAG, "Error creating file $targetPath", ex)
                throw CreateLocalFileException(targetPath.toString(), ex)
            } catch (ex: SecurityException) {
                Log_OC.e(TAG, "Error creating file $targetPath", ex)
                throw CreateLocalFileException(targetPath.toString(), ex)
            }

        BufferedInputStream(responseStream).use { bis ->
            outputStream.use { fos ->
                val buffer = ByteArray(BUFFER_SIZE)
                var bytesRead: Int
                while (bis.read(buffer).also { bytesRead = it } != -1) {
                    if (cancellationRequested.get()) throw OperationCancelledException()
                    fos.write(buffer, 0, bytesRead)
                }
            }
        }
    }

    private fun readMetadata(getMethod: GetMethod) {
        val modificationTime =
            getMethod.getResponseHeader("Last-Modified")
                ?: getMethod.getResponseHeader("last-modified")

        if (modificationTime != null) {
            modificationTimestamp = WebdavUtils.parseResponseDate(modificationTime)?.time ?: 0
        } else {
            Log_OC.e(TAG, "Could not read modification time from response downloading $remotePath")
        }

        etag = WebdavUtils.getEtagFromResponse(getMethod)
        if (etag.isEmpty()) {
            Log_OC.e(TAG, "Could not read eTag from response downloading $remotePath")
        }
    }

    private fun isSuccess(status: Int) = (status == HttpStatus.SC_OK)

    private val tmpPath: String
        get() = temporalFolderPath + remotePath
    // endregion

    // region public methods
    fun addProgressListener(listener: OnDatatransferProgressListener) {
        synchronized(dataTransferListeners) { dataTransferListeners.add(listener) }
    }

    fun removeProgressListener(listener: OnDatatransferProgressListener) {
        synchronized(dataTransferListeners) { dataTransferListeners.remove(listener) }
    }

    fun cancel() {
        cancellationRequested.set(true)
    }
    // endregion

    companion object {
        private val TAG = DownloadFileRemoteOperation::class.java.simpleName
        private const val BUFFER_SIZE = 4096
    }
}
