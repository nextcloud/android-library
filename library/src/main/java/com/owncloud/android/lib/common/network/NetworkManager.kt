/*
 * Nextcloud Android client application
 *
 * @author Alper Ozturk
 * Copyright (C) 2024 Alper Ozturk
 * Copyright (C) 2024 Nextcloud GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.owncloud.android.lib.common.network

import android.content.Context
import com.google.gson.Gson
import com.nextcloud.common.NextcloudClient
import com.nextcloud.common.User
import com.owncloud.android.lib.common.OwnCloudClientFactory
import com.owncloud.android.lib.common.utils.Log_OC
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.lang.reflect.Type

@Suppress("TooGenericExceptionCaught")
class NetworkManager(user: User, context: Context) {
    private val tag = "NetworkManager"

    private var client: NextcloudClient? = null

    init {
        try {
            client = OwnCloudClientFactory.createNextcloudClient(user, context)
        } catch (e: Throwable) {
            Log_OC.d(tag, "Error caught at creating NextCloud client")
        }
    }

    private val httpClient =
        OkHttpClient.Builder().addInterceptor(NetworkManagerInterceptor()).build()
    private val accessToken = client?.credentials
    private val gson = Gson()
    private val headers =
        Headers.Builder()
            .add("Accept", "application/json")
            .add("Authorization", "Bearer $accessToken")
            .add("ocs-apirequest", "true")
            .build()

    fun <T> get(
        endpoint: String,
        type: Type
    ): T? {
        if (client == null) {
            Log_OC.d(tag, "Trying to execute a remote operation with a null Nextcloud Client")
            return null
        }

        val url = "${client?.baseUri}$endpoint"

        val request =
            Request.Builder()
                .url(url)
                .headers(headers)
                .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log_OC.d(tag, "GET request couldn't fetched")
                return null
            }

            val json = response.body.string()
            Log_OC.d(tag, "GET response $endpoint: $json")
            return gson.fromJson(json, type)
        }
    }

    fun <T> delete(endpoint: String, type: Type): T? {
        if (client == null) {
            Log_OC.d(tag, "Trying to execute a remote operation with a null Nextcloud Client")
            return null
        }

        val url = "${client?.baseUri}$endpoint"

        val request = Request.Builder()
            .url(url)
            .delete()
            .headers(headers)
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log_OC.d(tag, "DELETE request couldn't be executed")
                return null
            }

            val json = response.body.string()
            Log_OC.d(tag, "DELETE response $endpoint: $json")
            return gson.fromJson(json, type)
        }
    }

    fun <T> post(
        endpoint: String,
        type: Type,
        jsonBody: String
    ): T? {
        if (client == null) {
            Log_OC.d(tag, "Trying to execute a remote operation with a null Nextcloud Client")
            return null
        }

        val url = "${client?.baseUri}$endpoint"

        val requestBody =
            jsonBody.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(url)
            .headers(headers)
            .post(requestBody)
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log_OC.d(tag, "POST request couldn't fetched")
                return null
            }

            val json = response.body.string()
            Log_OC.d(tag, "POST response $endpoint: $json")
            return gson.fromJson(json, type)
        }
    }
}
