/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2024 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.android.lib.resources.declarativeui

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.reflect.TypeToken
import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.GetMethod
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.ocs.ServerResponse
import com.owncloud.android.lib.resources.OCSRemoteOperation
import com.owncloud.android.lib.resources.declarativeui.DeclarativeUI
import org.apache.commons.httpclient.HttpStatus

/**
 * Get terms of service of an user
 */
class GetDeclarativeUiJsonOperation(val url: String) : OCSRemoteOperation<JsonArray>() {
    @Suppress("TooGenericExceptionCaught")
    override fun run(client: NextcloudClient): RemoteOperationResult<JsonArray> {
        var result: RemoteOperationResult<JsonArray>
        var getMethod: GetMethod? = null
        try {
            getMethod =
                GetMethod(
                    client.baseUri.toString() + url + JSON_FORMAT,
                    true
                )
            val status = client.execute(getMethod)
            if (status == HttpStatus.SC_OK) {
                val terms =
                    getServerResponse(
                        getMethod,
                        object : TypeToken<ServerResponse<JsonArray>>() {}
                    )?.ocs?.data

                val json = parseResult(getMethod.getResponseBodyAsString())

                if (terms != null) {
                    result = RemoteOperationResult(true, getMethod)
                    result.resultData = terms
                } else {
                    result = RemoteOperationResult(false, getMethod)
                }
            } else {
                result = RemoteOperationResult(false, getMethod)
            }
        } catch (e: Exception) {
            result = RemoteOperationResult(e)
            Log_OC.e(
                TAG,
                "Get terms failed: " + result.logMessage,
                result.exception
            )
        } finally {
            getMethod?.releaseConnection()
        }
        return result
    }

    fun parseResult(response: String) : DeclarativeUI {
        // val listType = object : TypeToken<>>() {}.type

        // val gson = GsonBuilder()
        //     .registerTypeAdapter(RichElement::class.java, RichElementTypeAdapter())
        //     .registerTypeAdapter(PreviewObject::class.java, PreviewObjectAdapter())
        //     .create()
        // val listType = object : TypeToken<MutableList<Activity?>?>() {
        // }.getType()
        
        return Gson().fromJson(response, DeclarativeUI::class.java)
    }

    companion object {
        private val TAG = GetDeclarativeUiJsonOperation::class.java.simpleName
    }
}
