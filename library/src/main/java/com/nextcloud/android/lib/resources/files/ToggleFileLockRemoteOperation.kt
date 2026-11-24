/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-FileCopyrightText: 2022-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.android.lib.resources.files

import com.nextcloud.common.NextcloudClient
import com.nextcloud.common.OkHttpMethodBase
import com.nextcloud.operations.LockMethod
import com.nextcloud.operations.UnlockMethod
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import org.apache.commons.httpclient.HttpStatus

/**
 * Lock or unlock a file.
 *
 * @param toLock `true` if file is to be locked, `false` if it is to be unlocked
 */
class ToggleFileLockRemoteOperation(
    private val toLock: Boolean,
    private val filePath: String
) : RemoteOperation<Void>() {
    @Suppress("TooGenericExceptionCaught")
    override fun run(client: NextcloudClient): RemoteOperationResult<Void> {
        val method = createMethod(client.getFilesDavUri(filePath))

        return try {
            val status = client.execute(method)

            RemoteOperationResult(isSuccess(status), method)
        } catch (e: Exception) {
            failure(e)
        } finally {
            method.releaseConnection()
        }
    }

    private fun createMethod(uri: String): OkHttpMethodBase {
        val method = if (toLock) LockMethod(uri, false) else UnlockMethod(uri, false)

        return method.apply { addRequestHeader(HEADER_USER_LOCK, HEADER_USER_LOCK_VALUE) }
    }

    @Suppress("DEPRECATION")
    private fun failure(e: Exception): RemoteOperationResult<Void> =
        RemoteOperationResult<Void>(e).also {
            Log_OC.e(TAG, "Lock toggle failed: ${it.logMessage}", it.exception)
        }

    private fun isSuccess(status: Int): Boolean =
        when (status) {
            HttpStatus.SC_OK -> true

            // used for "already unlocked" when trying to unlock
            HttpStatus.SC_PRECONDITION_FAILED -> !toLock

            else -> false
        }

    companion object {
        private val TAG = ToggleFileLockRemoteOperation::class.simpleName
        private const val HEADER_USER_LOCK = "X-User-Lock"
        private const val HEADER_USER_LOCK_VALUE = "1"
    }
}
