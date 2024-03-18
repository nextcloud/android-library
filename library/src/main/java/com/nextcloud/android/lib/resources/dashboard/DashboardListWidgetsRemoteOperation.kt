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

class DashboardListWidgetsRemoteOperation : OCSRemoteOperation<Map<String, DashboardWidget>>() {
    override fun run(client: NextcloudClient): RemoteOperationResult<Map<String, DashboardWidget>> {
        lateinit var result: RemoteOperationResult<Map<String, DashboardWidget>>
        lateinit var get: GetMethod

        try {
            get = GetMethod(client.baseUri.toString() + LIST_ENDPOINT + JSON_FORMAT, true)
            val status = client.execute(get)

            if (status == HttpStatus.SC_OK) {
                val list =
                    getServerResponse(
                        get,
                        object : TypeToken<ServerResponse<HashMap<String, DashboardWidget>>>() {}
                    )?.ocs?.data

                if (list != null) {
                    result = RemoteOperationResult<Map<String, DashboardWidget>>(true, get)
                    result.resultData = list
                } else {
                    result = RemoteOperationResult<Map<String, DashboardWidget>>(false, get)
                }
            } else {
                result = RemoteOperationResult<Map<String, DashboardWidget>>(false, get)
            }
        } catch (e: IOException) {
            result = RemoteOperationResult<Map<String, DashboardWidget>>(e)
        }

        return result
    }

    companion object {
        const val LIST_ENDPOINT = "/ocs/v2.php/apps/dashboard/api/v1/widgets"
    }
}
