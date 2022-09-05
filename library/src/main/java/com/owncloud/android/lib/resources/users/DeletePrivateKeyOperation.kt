/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2022 Tobias Kaminsky
 *   Copyright (C) 2022 Nextcloud GmbH
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
package com.owncloud.android.lib.resources.users

import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import org.apache.commons.httpclient.HttpStatus
import org.apache.commons.httpclient.methods.DeleteMethod
import java.io.IOException

/**
 * Remote operation performing to delete the private key for an user
 */
class DeletePrivateKeyOperation : RemoteOperation<Void>() {
    /**
     * @param client Client object
     */
    override fun run(client: OwnCloudClient): RemoteOperationResult<Void> {
        var postMethod: DeleteMethod? = null
        var result: RemoteOperationResult<Void>
        try {
            // remote request
            postMethod = DeleteMethod(client.baseUri.toString() + PRIVATE_KEY_URL)
            postMethod.addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE)
            val status =
                client.executeMethod(postMethod, SYNC_READ_TIMEOUT, SYNC_CONNECTION_TIMEOUT)
            result = RemoteOperationResult<Void>(status == HttpStatus.SC_OK, postMethod)
            client.exhaustResponse(postMethod.responseBodyAsStream)
        } catch (e: IOException) {
            result = RemoteOperationResult<Void>(e)
            Log_OC.e(TAG, "Deletion of private key failed: " + result.logMessage, result.exception)
        } finally {
            postMethod?.releaseConnection()
        }
        return result
    }

    companion object {
        private val TAG = DeletePrivateKeyOperation::class.java.simpleName
        private const val SYNC_READ_TIMEOUT = 40000
        private const val SYNC_CONNECTION_TIMEOUT = 5000
        private const val PRIVATE_KEY_URL =
            "/ocs/v2.php/apps/end_to_end_encryption/api/v1/private-key"
    }
}
