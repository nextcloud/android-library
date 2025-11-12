/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2025 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.assistant.chat

import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.DeleteMethod
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import org.apache.commons.httpclient.HttpStatus

class DeleteConversationRemoteOperation(
    private val sessionId: String
) : RemoteOperation<Void>() {
    @Suppress("TooGenericExceptionCaught")
    override fun run(client: NextcloudClient): RemoteOperationResult<Void> {
        val deleteMethod =
            DeleteMethod(
                client.baseUri.toString() + "$BASE_URL/delete_session?sessionId=$sessionId",
                true
            )
        val status = deleteMethod.execute(client)

        return try {
            if (status == HttpStatus.SC_OK) {
                RemoteOperationResult(true, deleteMethod)
            } else {
                RemoteOperationResult(false, deleteMethod)
            }
        } catch (e: Exception) {
            Log_OC.e(TAG, "delete session: ", e)
            RemoteOperationResult(false, deleteMethod)
        } finally {
            deleteMethod.releaseConnection()
        }
    }

    companion object {
        private const val TAG = "DeleteConversationRemoteOperation"
        private const val BASE_URL = "/ocs/v2.php/apps/assistant/chat"
    }
}
