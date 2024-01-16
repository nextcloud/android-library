/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2017 Tobias Kaminsky
 *   Copyright (C) 2017 Nextcloud GmbH
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
package com.owncloud.android.lib.resources.e2ee

import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import org.apache.commons.httpclient.HttpStatus
import org.apache.commons.httpclient.methods.DeleteMethod

/**
 * Unlock a file
 */
class UnlockFileV1RemoteOperation(
    private val localId: Long,
    private val token: String
) : RemoteOperation<Void>() {
    /**
     * @param client Client object
     */
    @Deprecated("Deprecated in Java")
    @Suppress("Detekt.TooGenericExceptionCaught")
    override fun run(client: OwnCloudClient): RemoteOperationResult<Void> {
        var result: RemoteOperationResult<Void>
        var deleteMethod: DeleteMethod? = null
        try {
            // remote request
            deleteMethod = DeleteMethod(client.baseUri.toString() + LOCK_FILE_URL + localId)
            deleteMethod.addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE)
            deleteMethod.addRequestHeader(CONTENT_TYPE, FORM_URLENCODED)
            deleteMethod.addRequestHeader(E2E_TOKEN, token)
            val status =
                client.executeMethod(deleteMethod, SYNC_READ_TIMEOUT, SYNC_CONNECTION_TIMEOUT)
            result = RemoteOperationResult(status == HttpStatus.SC_OK, deleteMethod)
            client.exhaustResponse(deleteMethod.responseBodyAsStream)
        } catch (e: Exception) {
            result = RemoteOperationResult(e)
            Log_OC.e(
                TAG,
                "Unlock file with id " + localId + " failed: " + result.logMessage,
                result.exception
            )
        } finally {
            deleteMethod?.releaseConnection()
        }
        return result
    }

    companion object {
        private val TAG = UnlockFileV1RemoteOperation::class.java.simpleName
        private const val SYNC_READ_TIMEOUT = 40000
        private const val SYNC_CONNECTION_TIMEOUT = 5000
        private const val LOCK_FILE_URL = "/ocs/v2.php/apps/end_to_end_encryption/api/v1/lock/"
    }
}
