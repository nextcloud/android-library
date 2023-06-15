/*
 * Nextcloud Android client application
 *
 * @author Tobias Kaminsky
 * Copyright (C) 2023 Tobias Kaminsky
 * Copyright (C) 2023 Nextcloud GmbH
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *  
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 *  
 */

package com.owncloud.android.lib.common.operations

import android.accounts.Account
import android.accounts.AccountManager
import android.accounts.AccountsException
import android.content.Context
import com.nextcloud.common.User
import com.owncloud.android.lib.common.OwnCloudAccount
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.OwnCloudClientFactory
import com.owncloud.android.lib.common.OwnCloudClientManagerFactory
import com.owncloud.android.lib.common.accounts.AccountUtils
import com.owncloud.android.lib.common.operations.RemoteOperationResult.ResultCode
import com.owncloud.android.lib.common.utils.Log_OC
import java.io.IOException

@Deprecated("Use NextcloudRemoteOperation instead")
abstract class LegacyRemoteOperation<T> : RemoteOperation<T>() {
    private val TAG = LegacyRemoteOperation::class.java.simpleName

    /**
     * Abstract method to implement the operation in derived classes.
     */
    @Deprecated("Use NextcloudRemoteOperation instead")
    protected abstract fun run(client: OwnCloudClient): RemoteOperationResult<T>

    /**
     * Synchronously executes the remote operation on the received ownCloud account.
     *
     * Do not call this method from the main thread.
     *
     * This method should be used whenever an ownCloud account is available, instead of
     * [.execute].
     *
     * @param account   ownCloud account in remote ownCloud server to reach during the
     * execution of the operation.
     * @param context   Android context for the component calling the method.
     * @return Result of the operation.
     */
    @Deprecated("")
    open fun execute(account: Account?, context: Context?): RemoteOperationResult<T> {
        requireNotNull(account) { "Trying to execute a remote operation with a NULL Account" }
        requireNotNull(context) { "Trying to execute a remote operation with a NULL Context" }
        mAccount = account
        mContext = context.applicationContext
        mClient = try {
            val ocAccount = OwnCloudAccount(mAccount, mContext)
            OwnCloudClientManagerFactory.getDefaultSingleton().getClientFor(ocAccount, mContext)
        } catch (e: Exception) {
            Log_OC.e(TAG, "Error while trying to access to " + mAccount.name, e)
            return RemoteOperationResult(e)
        }
        return run(mClient)
    }

    /**
     * This is a transitional wrapper around [.execute]
     * using modern [User] interface instead of platform [Account]
     */
    @Deprecated("")
    open fun execute(user: User, context: Context): RemoteOperationResult<T> {
        return execute(user.toPlatformAccount(), context)
    }

    /**
     * Synchronously executes the remote operation
     *
     *
     * Do not call this method from the main thread.
     *
     * @param client Client object to reach an ownCloud server during the execution of the operation.
     * @return Result of the operation.
     */
    @Deprecated("")
    open fun execute(client: OwnCloudClient): RemoteOperationResult<T> {
        requireNotNull(client) { "Trying to execute a remote operation with a NULL OwnCloudClient" }
        mClient = client
        return run(client)
    }

    /**
     * Asynchronous execution of the operation
     * started by [RemoteOperation.execute],
     * and result posting.
     *
     * TODO refactor && clean the code; now it's a mess
     */
    override fun run() {
        var result: RemoteOperationResult<T>? = null
        var repeat: Boolean
        do {
            try {
                if (mClient == null) {
                    mClient = if (mAccount != null && mContext != null) {
                        /** DEPRECATED BLOCK - will be removed at version 1.0  */
                        if (mCallerActivity != null) {
                            OwnCloudClientFactory.createOwnCloudClient(
                                mAccount, mContext, mCallerActivity
                            )
                        } else {
                            /** EOF DEPRECATED  */
                            val ocAccount = OwnCloudAccount(mAccount, mContext)
                            OwnCloudClientManagerFactory.getDefaultSingleton()
                                .getClientFor(ocAccount, mContext)
                        }
                    } else {
                        throw IllegalStateException(
                            "Trying to run a remote operation " +
                                "asynchronously with no client instance or account"
                        )
                    }
                }
            } catch (e: IOException) {
                Log_OC.e(
                    TAG, "Error while trying to access to " + mAccount.name,
                    AccountsException(
                        "I/O exception while trying to authorize the account",
                        e
                    )
                )
                result = RemoteOperationResult(e)
            } catch (e: AccountsException) {
                Log_OC.e(TAG, "Error while trying to access to " + mAccount.name, e)
                result = RemoteOperationResult(e)
            }
            if (result == null) result = run(mClient)
            repeat = false
            /** DEPRECATED BLOCK - will be removed at version 1.0 ; don't trust in this code
             * to trigger authentication update  */
            if (mCallerActivity != null && mAccount != null && mContext != null &&
                !result.isSuccess && ResultCode.UNAUTHORIZED == result.code
            ) {
                /// possible fail due to lack of authorization
                // in an operation performed in foreground
                val cred = mClient.credentials
                if (cred != null) {
                    /// confirmed : unauthorized operation
                    val am = AccountManager.get(mContext)
                    if (cred.authTokenExpires()) {
                        am.invalidateAuthToken(
                            mAccount.type,
                            cred.authToken
                        )
                    } else {
                        am.clearPassword(mAccount)
                    }
                    mClient = null
                    // when repeated, the creation of a new OwnCloudClient after erasing the saved
                    // credentials will trigger the login activity
                    repeat = true
                    result = null
                }
            }
            /** EOF DEPRECATED BLOCK  */
        } while (repeat)
        if (mAccount != null && mContext != null) {
            // Save Client Cookies
            AccountUtils.saveClient(mClient, mAccount, mContext)
        }
        val resultToSend = result
        if (mListenerHandler != null && mListener != null) {
            mListenerHandler.post {
                mListener.onRemoteOperationFinish(
                    this,
                    resultToSend
                )
            }
        }
    }
}
