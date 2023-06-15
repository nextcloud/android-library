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
import android.content.Context
import com.nextcloud.common.NextcloudClient
import com.nextcloud.common.User
import com.owncloud.android.lib.common.OwnCloudAccount
import com.owncloud.android.lib.common.OwnCloudClientManagerFactory
import com.owncloud.android.lib.common.utils.Log_OC

abstract class NextcloudRemoteOperation<T> : RemoteOperation<T>() {
    private val TAG = NextcloudRemoteOperation::class.java.simpleName
    
    /**
     * Synchronously executes the remote operation on the received ownCloud account.
     *
     *
     * Do not call this method from the main thread.
     *
     *
     * This method should be used whenever an ownCloud account is available, instead of
     * [.execute].
     *
     * @param account ownCloud account in remote ownCloud server to reach during the
     * execution of the operation.
     * @param context Android context for the component calling the method.
     * @return Result of the operation.
     */
    private fun executeNextcloudClient(account: Account, context: Context): RemoteOperationResult<T> {
        mAccount = account
        mContext = context.applicationContext
        clientNew = try {
            val ocAccount = OwnCloudAccount(mAccount, mContext)
            OwnCloudClientManagerFactory.getDefaultSingleton()
                .getNextcloudClientFor(ocAccount, mContext)
        } catch (e: Exception) {
            Log_OC.e(TAG, "Error while trying to access to " + mAccount.name, e)
            return RemoteOperationResult(e)
        }
        return run(clientNew)
    }

    /**
     * This is a transitional wrapper around [.executeNextcloudClient]
     * using modern [User] interface instead of platform [Account]
     */
    fun executeNextcloudClient(user: User, context: Context): RemoteOperationResult<T> {
        return executeNextcloudClient(user.toPlatformAccount(), context)
    }

    /**
     * Synchronously executes the remote operation
     *
     * Do not call this method from the main thread.
     *
     * @param client    Client object to reach an ownCloud server during the execution of
     * the operation.
     * @return Result of the operation.
     */
    fun execute(client: NextcloudClient): RemoteOperationResult<T> {
        clientNew = client
        return run(client)
    }

    abstract fun run(client: NextcloudClient): RemoteOperationResult<T>
}
