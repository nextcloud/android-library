/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2022-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.users

import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.DeleteMethod
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import java.io.IOException
import java.net.HttpURLConnection

/**
 * Remote operation performing to delete the private key for an user
 */
class DeletePrivateKeyRemoteOperation : RemoteOperation<Void>() {
    /**
     * @param client Client object
     */
    override fun run(client: NextcloudClient): RemoteOperationResult<Void> {
        var postMethod: DeleteMethod? = null
        var result: RemoteOperationResult<Void>
        try {
            // remote request
            postMethod =
                DeleteMethod(
                    client.baseUri.toString() + PRIVATE_KEY_URL,
                    true
                )
            val status = client.execute(postMethod)
            result = RemoteOperationResult<Void>(status == HttpURLConnection.HTTP_OK, postMethod)
        } catch (e: IOException) {
            result = RemoteOperationResult<Void>(e)
            Log_OC.e(TAG, "Deletion of private key failed: " + result.logMessage, result.exception)
        } finally {
            postMethod?.releaseConnection()
        }
        return result
    }

    companion object {
        private val TAG = DeletePrivateKeyRemoteOperation::class.java.simpleName
        private const val PRIVATE_KEY_URL =
            "/ocs/v2.php/apps/end_to_end_encryption/api/v1/private-key"
    }
}
