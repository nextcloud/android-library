/*
 * Nextcloud Android Library
 *
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
class ToggleFileLockRemoteOperation(private val toLock: Boolean, private val filePath: String) :
    RemoteOperation<Void>() {
    @Suppress("TooGenericExceptionCaught")
    override fun run(client: NextcloudClient): RemoteOperationResult<Void> {
        var method: OkHttpMethodBase? = null
        var result: RemoteOperationResult<Void>?

        try {
            // remote request
            val uri = client.getFilesDavUri(filePath)
            method =
                when (toLock) {
                    true -> LockMethod(uri, false)
                    false -> UnlockMethod(uri, false)
                }
            method.addRequestHeader(HEADER_USER_LOCK, HEADER_USER_LOCK_VALUE)

            val status: Int = client.execute(method)
            if (isSuccess(status)) {
                result = RemoteOperationResult(true, method)
            } else {
                result = RemoteOperationResult(false, method)
                method.releaseConnection()
            }
        } catch (e: Exception) {
            result = RemoteOperationResult(e)
            Log_OC.e(TAG, "Lock toggle failed: " + result.logMessage, result.exception)
        } finally {
            method?.releaseConnection()
        }

        return result!!
    }

    private fun isSuccess(status: Int): Boolean =
        when (status) {
            HttpStatus.SC_OK -> true
            HttpStatus.SC_LOCKED -> toLock // treat "already locked" as success when trying to lock
            HttpStatus.SC_PRECONDITION_FAILED -> !toLock // used for "already unlocked" when trying to unlock
            else -> false
        }

    companion object {
        private val TAG = ToggleFileLockRemoteOperation::class.simpleName
        private const val HEADER_USER_LOCK = "X-User-Lock"
        private const val HEADER_USER_LOCK_VALUE = "1"
    }
}
