/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2022-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022-2024 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.android.lib.resources.dashboard

import com.google.gson.reflect.TypeToken
import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.GetMethod
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.ocs.ServerResponse
import com.owncloud.android.lib.resources.OCSRemoteOperation
import org.apache.commons.httpclient.HttpStatus
import java.io.IOException

class DashboardGetWidgetItemsRemoteOperation(val id: String, private val limitSize: Int) :
    OCSRemoteOperation<HashMap<String, List<DashboardWidgetItem>>>() {
    override fun run(client: NextcloudClient): RemoteOperationResult<HashMap<String, List<DashboardWidgetItem>>> {
        lateinit var result: RemoteOperationResult<HashMap<String, List<DashboardWidgetItem>>>
        lateinit var get: GetMethod

        try {
            get = GetMethod(client.baseUri.toString() + ENDPOINT + JSON_FORMAT, true)
            get.setQueryString(mapOf(Pair("widgets[]", id), Pair(LIMIT, limitSize.toString())))
            val status = client.execute(get)

            if (status == HttpStatus.SC_OK) {
                val list =
                    getServerResponse(
                        get,
                        object :
                            TypeToken<ServerResponse<HashMap<String, List<DashboardWidgetItem>>>>() {}
                    ).ocs.data

                result =
                    RemoteOperationResult<HashMap<String, List<DashboardWidgetItem>>>(true, get)
                result.resultData = list
            } else {
                result =
                    RemoteOperationResult<HashMap<String, List<DashboardWidgetItem>>>(false, get)
            }
        } catch (e: IOException) {
            result = RemoteOperationResult<HashMap<String, List<DashboardWidgetItem>>>(e)
        }

        return result
    }

    companion object {
        const val ENDPOINT = "/ocs/v2.php/apps/dashboard/api/v1/widget-items"
        const val LIMIT = "limit"
    }
}
