/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Your Name <your@email.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package com.nextcloud.android.lib.resources.users

import com.nextcloud.android.lib.resources.users.model.UserInfoResponse
import com.owncloud.android.lib.ocs.OcsKotlinResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

private const val ENDPOINT_USER = "ocs/v2.php/cloud/user"
private const val QUERY_FORMAT = "format"
private const val OCS_API_REQUEST_HEADER = "OCS-APIREQUEST: true"

/**
 * Retrofit definition of the OCS user endpoint. The caller supplies a [retrofit2.Retrofit]
 * whose OkHttpClient adds authentication, e.g. one derived from [com.nextcloud.common.NextcloudClient.client].
 */
interface UserApi {
    @Headers(OCS_API_REQUEST_HEADER)
    @GET(ENDPOINT_USER)
    fun getUser(
        @Query(QUERY_FORMAT) format: String
    ): Call<OcsKotlinResponse<UserInfoResponse>>

    @Headers(OCS_API_REQUEST_HEADER)
    @GET(ENDPOINT_USER)
    suspend fun fetchUser(
        @Query(QUERY_FORMAT) format: String
    ): OcsKotlinResponse<UserInfoResponse>
}
