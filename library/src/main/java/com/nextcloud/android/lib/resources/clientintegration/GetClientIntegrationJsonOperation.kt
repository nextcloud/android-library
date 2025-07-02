/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2025 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.android.lib.resources.clientintegration

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.reflect.TypeToken
import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.GetMethod
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.ocs.ServerResponse
import com.owncloud.android.lib.resources.OCSRemoteOperation
import org.apache.commons.httpclient.HttpStatus

/**
 * Get client integration
 */
class GetClientIntegrationJsonOperation(
    val url: String
) : OCSRemoteOperation<JsonArray>() {
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
                "Get client integration failed: " + result.logMessage,
                result.exception
            )
        } finally {
            getMethod?.releaseConnection()
        }
        return result
    }

    fun parseResult(response: String): ClientIntegrationUI {
        val gson =
            GsonBuilder()
                .registerTypeHierarchyAdapter(Element::class.java, ElementTypeAdapter())
                .create()

        return gson.fromJson(response, ClientIntegrationUI::class.java)
    }

    companion object {
        private val TAG = GetClientIntegrationJsonOperation::class.java.simpleName
    }
}
