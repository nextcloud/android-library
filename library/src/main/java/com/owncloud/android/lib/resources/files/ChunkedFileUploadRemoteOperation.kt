/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2015 ownCloud Inc.
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files

import androidx.annotation.VisibleForTesting
import androidx.core.text.isDigitsOnly
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.network.ChunkFromFileChannelRequestEntity
import com.owncloud.android.lib.common.network.ProgressiveDataTransfer
import com.owncloud.android.lib.common.network.WebdavEntry
import com.owncloud.android.lib.common.network.WebdavUtils
import com.owncloud.android.lib.common.operations.OperationCancelledException
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler
import org.apache.commons.httpclient.methods.PutMethod
import org.apache.commons.httpclient.params.HttpMethodParams
import org.apache.jackrabbit.webdav.DavConstants
import org.apache.jackrabbit.webdav.MultiStatus
import org.apache.jackrabbit.webdav.client.methods.MkColMethod
import org.apache.jackrabbit.webdav.client.methods.MoveMethod
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod
import java.io.Closeable
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.channels.FileChannel
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

@Suppress("LongParameterList")
class ChunkedFileUploadRemoteOperation @JvmOverloads constructor(
    storagePath: String?,
    remotePath: String?,
    mimeType: String?,
    requiredEtag: String?,
    lastModificationTimestamp: Long,
    private val onWifiConnection: Boolean,
    token: String? = null,
    creationTimestamp: Long? = null,
    disableRetries: Boolean = true
) : UploadFileRemoteOperation(
    storagePath,
    remotePath,
    mimeType,
    requiredEtag,
    lastModificationTimestamp,
    creationTimestamp,
    token,
    disableRetries
) {
    @Suppress("VariableNaming", "MagicNumber")
    @JvmField
    val ASSEMBLE_TIME_MIN: Int = 30 * 1000 // 30s

    @Suppress("VariableNaming", "MagicNumber")
    @JvmField
    val ASSEMBLE_TIME_MAX: Int = 30 * 60 * 1000 // 30min

    @Suppress("VariableNaming", "MagicNumber")
    @JvmField
    val ASSEMBLE_TIME_PER_GB: Int = 3 * 60 * 1000 // 3 min

    private lateinit var uploadFolderUri: String
    private lateinit var destinationUri: String
    private var moveMethod: MoveMethod? = null

    constructor(
        storagePath: String?,
        remotePath: String?,
        mimeType: String?,
        requiredEtag: String?,
        lastModificationTimestamp: Long,
        creationTimestamp: Long?,
        onWifiConnection: Boolean,
        disableRetries: Boolean
    ) : this(
        storagePath,
        remotePath,
        mimeType,
        requiredEtag,
        lastModificationTimestamp,
        onWifiConnection,
        null,
        creationTimestamp,
        disableRetries
    )

    @Suppress("TooGenericExceptionCaught")
    override fun run(client: OwnCloudClient): RemoteOperationResult<String> {
        val oldRetryHandler = client.params
            .getParameter(HttpMethodParams.RETRY_HANDLER) as? DefaultHttpMethodRetryHandler

        return try {
            if (disableRetries) {
                // prevent that uploads are retried automatically by network library
                client.params.setParameter(HttpMethodParams.RETRY_HANDLER, DefaultHttpMethodRetryHandler(0, false))
            }

            uploadAndAssemble(client)
        } catch (e: Exception) {
            cancelledOrFailed(e)
        } finally {
            if (disableRetries) {
                // reset previous retry handler
                client.params.setParameter(HttpMethodParams.RETRY_HANDLER, oldRetryHandler)
            }
        }
    }

    private fun uploadAndAssemble(client: OwnCloudClient): RemoteOperationResult<String> {
        val file = File(localPath)
        val userId = client.userId

        uploadFolderUri = "${client.uploadUri}/$userId/${FileUtils.md5Sum(file)}"
        destinationUri = "${client.davUri}/files/$userId${WebdavUtils.encodePath(remotePath)}"

        createUploadFolder(client)

        val listChunks = PropFindMethod(uploadFolderUri, WebdavUtils.getChunksPropSet(), DavConstants.DEPTH_1)
        client.executeMethod(listChunks)

        if (!listChunks.succeeded()) {
            return RemoteOperationResult(false, listChunks)
        }

        val uploaded = uploadedChunks(client, listChunks.responseBodyAsMultiStatus)

        return uploadRemainingChunks(client, file, uploaded) ?: assemble(client, file)
    }

    private fun createUploadFolder(client: OwnCloudClient) {
        val createFolder = MkColMethod(uploadFolderUri)
        createFolder.addRequestHeader(DESTINATION_HEADER, destinationUri)
        client.executeMethod(createFolder, CREATE_FOLDER_READ_TIMEOUT, CREATE_FOLDER_CONNECTION_TIMEOUT)
    }

    private fun uploadedChunks(client: OwnCloudClient, dataInServer: MultiStatus): UploadedChunks {
        val uploadPath = requireNotNull(client.uploadUri.path)
        var nextByte = 0L
        var lastId = 0

        for (response in dataInServer.responses) {
            val entry = WebdavEntry(response, uploadPath)
            val chunkId = entry.chunkId() ?: continue

            lastId = max(lastId, chunkId)
            nextByte += entry.contentLength
        }

        return UploadedChunks(nextByte, lastId)
    }

    /** Id of the chunk this entry holds, or `null` for any object not matching the expected chunk name. */
    private fun WebdavEntry.chunkId(): Int? = name
        ?.takeIf { !isDirectory && it.length <= CHUNK_NAME_LENGTH && it.isDigitsOnly() }
        ?.toIntOrNull()

    private fun uploadRemainingChunks(
        client: OwnCloudClient,
        file: File,
        uploaded: UploadedChunks
    ): RemoteOperationResult<String>? {
        val chunkSize = if (onWifiConnection) CHUNK_SIZE_WIFI else CHUNK_SIZE_MOBILE
        var nextByte = uploaded.nextByte
        var lastId = uploaded.lastId
        var failure: RemoteOperationResult<String>? = null

        while (failure == null && nextByte + 1 < file.length()) {
            // determine size of next chunk
            val chunk = calcNextChunk(file.length(), ++lastId, nextByte, chunkSize)
            val chunkResult = uploadChunk(client, chunk)

            failure = when {
                !chunkResult.isSuccess -> chunkResult
                cancellationRequested.get() -> RemoteOperationResult(OperationCancelledException())
                else -> null
            }

            nextByte += chunk.length
        }

        return failure
    }

    private fun assemble(client: OwnCloudClient, file: File): RemoteOperationResult<String> {
        val move = MoveMethod(uploadFolderUri + ASSEMBLED_FILE_SUFFIX, destinationUri, true)
        moveMethod = move

        move.addRequestHeader(OC_X_OC_MTIME_HEADER, lastModificationTimestamp.toString())
        creationTimestamp?.takeIf { it > 0 }?.let { move.addRequestHeader(OC_X_OC_CTIME_HEADER, it.toString()) }
        token?.let { move.addRequestHeader(E2E_TOKEN, it) }

        val status = client.executeMethod(move, calculateAssembleTimeout(file), DO_NOT_CHANGE_DEFAULT)

        return RemoteOperationResult(isSuccess(status), move)
    }

    @Throws(IOException::class)
    private fun uploadChunk(client: OwnCloudClient, chunk: Chunk): RemoteOperationResult<String> {
        val file = File(localPath)
        var raf: RandomAccessFile? = null
        var channel: FileChannel? = null

        return try {
            raf = RandomAccessFile(file, "r")
            channel = raf.channel
            entity = ChunkFromFileChannelRequestEntity(channel, mimeType, chunk.start, chunk.length, file)

            synchronized(dataTransferListeners) {
                (entity as ProgressiveDataTransfer).addDataTransferProgressListeners(dataTransferListeners)
            }

            // pad chunk name to 6 digits
            val chunkName = String.format(Locale.ROOT, "%0${CHUNK_NAME_LENGTH}d", chunk.id)

            putMethod?.releaseConnection() // let the connection available for other methods

            val put = createPutMethod("$uploadFolderUri/$chunkName")
            put.addRequestHeader(DESTINATION_HEADER, destinationUri)
            token?.let { put.addRequestHeader(E2E_TOKEN, it) }

            val status = client.executeMethod(put)
            val result = RemoteOperationResult<String>(isSuccess(status), put)

            client.exhaustResponse(put.responseBodyAsStream)
            Log_OC.d(
                TAG,
                "Upload of $localPath to $remotePath, chunk id: ${chunk.id} from ${chunk.start} " +
                    "size: ${chunk.length}, HTTP result status $status"
            )

            result
        } finally {
            channel.closeQuietly("Error closing file channel!")
            raf.closeQuietly("Error closing file access!")
            putMethod?.releaseConnection()
        }
    }

    private fun createPutMethod(uri: String): PutMethod {
        val put = PutMethod(uri)
        putMethod = put
        put.requestEntity = entity

        if (cancellationRequested.get()) {
            put.abort() // next method will throw an exception
        }

        return put
    }

    private fun cancelledOrFailed(e: Exception): RemoteOperationResult<String> = when {
        putMethod?.isAborted == true || moveMethod?.isAborted == true ->
            cancellationReason
                ?.takeIf { cancellationRequested.get() }
                ?.let { RemoteOperationResult(it) }
                ?: RemoteOperationResult(OperationCancelledException())

        else -> RemoteOperationResult(e)
    }

    private fun Closeable?.closeQuietly(errorMessage: String) {
        try {
            this?.close()
        } catch (e: IOException) {
            Log_OC.e(TAG, errorMessage, e)
        }
    }

    @VisibleForTesting
    fun calculateAssembleTimeout(file: File): Int {
        val fileSizeInGb = file.length() / BYTES_PER_GB

        return max(ASSEMBLE_TIME_MIN, min((ASSEMBLE_TIME_PER_GB * fileSizeInGb).toInt(), ASSEMBLE_TIME_MAX))
    }

    private data class UploadedChunks(val nextByte: Long, val lastId: Int)

    companion object {
        const val CHUNK_SIZE_MOBILE: Long = 10240000
        const val CHUNK_SIZE_WIFI: Long = 40960000
        const val DESTINATION_HEADER: String = "Destination"
        const val CHUNK_NAME_LENGTH: Int = 6

        private const val ASSEMBLED_FILE_SUFFIX = "/.file"
        private const val CREATE_FOLDER_READ_TIMEOUT = 30_000
        private const val CREATE_FOLDER_CONNECTION_TIMEOUT = 5_000
        private const val DO_NOT_CHANGE_DEFAULT = -1
        private const val BYTES_PER_GB = 1e9
        private val TAG = ChunkedFileUploadRemoteOperation::class.java.simpleName

        internal fun calcNextChunk(fileSize: Long, chunkId: Int, startByte: Long, chunkSize: Long): Chunk {
            require(chunkId >= 0 && chunkId.toString().length <= CHUNK_NAME_LENGTH) {
                "chunkId must not exceed length specified in CHUNK_NAME_LENGTH ($CHUNK_NAME_LENGTH)"
            }

            val length = if (startByte + chunkSize > fileSize) fileSize - startByte else chunkSize

            return Chunk(chunkId, startByte, length)
        }
    }
}
