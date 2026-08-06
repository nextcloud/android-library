/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-FileCopyrightText: 2017 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.users

import com.google.gson.reflect.TypeToken
import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.GetMethod
import com.owncloud.android.lib.common.Quota
import com.owncloud.android.lib.common.UserInfo
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.ocs.ServerResponse
import com.owncloud.android.lib.resources.OCSRemoteOperation
import org.apache.commons.httpclient.HttpStatus

/**
 * Gets information (id, display name, e-mail address and many other things) about the user logged in.
 */
class GetUserInfoRemoteOperation : OCSRemoteOperation<UserInfo>() {
    @Suppress("TooGenericExceptionCaught")
    override fun run(client: NextcloudClient): RemoteOperationResult<UserInfo> {
        val getMethod = GetMethod(client.baseUri.toString() + OCS_ROUTE_SELF + JSON_FORMAT, true)
        getMethod.addRequestHeader(CONTENT_TYPE, JSON_UTF8_ENCODED)

        return try {
            val status = client.execute(getMethod)
            if (status != HttpStatus.SC_OK) {
                return failure(getMethod, status)
            }

            parseUserInfo(getMethod)?.let { success(getMethod, it) } ?: missingUserInfoFailure(getMethod)
        } catch (e: Exception) {
            failure(e)
        } finally {
            getMethod.releaseConnection()
        }
    }

    private fun parseUserInfo(method: GetMethod): UserInfo? =
        getServerResponse(method, object : TypeToken<ServerResponse<UserInfo>>() {})?.ocs?.data

    private fun success(
        method: GetMethod,
        userInfo: UserInfo
    ): RemoteOperationResult<UserInfo> =
        RemoteOperationResult<UserInfo>(true, method).apply {
            resultData = userInfo.withResolvedQuota()
        }

    /**
     * A missing quota, or a quota of 0, means the server did not report one — it is not an actual limit of zero.
     */
    private fun UserInfo.withResolvedQuota(): UserInfo =
        copy(quota = quota?.takeIf { it.quota != 0L } ?: Quota(QUOTA_LIMIT_INFO_NOT_AVAILABLE))

    private fun failure(
        method: GetMethod,
        status: Int
    ): RemoteOperationResult<UserInfo> {
        val response = method.getResponseBodyAsString()
        val message = if (response.isEmpty()) "" else "; response message: $response"
        Log_OC.e(TAG, "Failed response while getting user information, status code: $status$message")
        return RemoteOperationResult(false, method)
    }

    private fun missingUserInfoFailure(method: GetMethod): RemoteOperationResult<UserInfo> {
        Log_OC.e(TAG, "User information missing in response")
        return RemoteOperationResult(false, method)
    }

    @Suppress("DEPRECATION")
    private fun failure(e: Exception): RemoteOperationResult<UserInfo> =
        RemoteOperationResult<UserInfo>(e).also {
            Log_OC.e(TAG, "Exception while getting user information: " + it.logMessage, it.exception)
        }

    companion object {
        private val TAG = GetUserInfoRemoteOperation::class.java.simpleName

        private const val OCS_ROUTE_SELF = "/ocs/v2.php/cloud/user"

        private const val JSON_UTF8_ENCODED = "application/json; charset=utf-8"

        /**
         * Quota return value for unlimited space.
         */
        const val SPACE_UNLIMITED = -3L

        /**
         * Quota return value for quota information not available.
         */
        const val QUOTA_LIMIT_INFO_NOT_AVAILABLE = Long.MIN_VALUE
    }
}
