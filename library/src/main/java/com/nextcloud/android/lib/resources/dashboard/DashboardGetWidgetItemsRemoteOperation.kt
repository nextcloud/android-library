/*
 * Nextcloud Android client application
 *
 * @author Tobias Kaminsky
 * Copyright (C) 2022 Tobias Kaminsky
 * Copyright (C) 2022 Nextcloud GmbH
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

package com.nextcloud.android.lib.resources.dashboard

import com.google.gson.reflect.TypeToken
import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.GetMethod
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.ocs.ServerResponse
import com.owncloud.android.lib.resources.OCSNextcloudRemoteOperation
import org.apache.commons.httpclient.HttpStatus
import java.io.IOException

class DashboardGetWidgetItemsRemoteOperation(val id: String, private val limitSize: Int) :
    OCSNextcloudRemoteOperation<HashMap<String, List<DashboardWidgetItem>>>() {

    override fun run(client: NextcloudClient): RemoteOperationResult<HashMap<String, List<DashboardWidgetItem>>> {
        lateinit var result: RemoteOperationResult<HashMap<String, List<DashboardWidgetItem>>>
        lateinit var get: GetMethod

        try {
            get = GetMethod(client.baseUri.toString() + ENDPOINT + JSON_FORMAT, true)
            get.setQueryString(mapOf(Pair("widgets[]", id), Pair(LIMIT, limitSize.toString())))
            val status = client.execute(get)

            if (status == HttpStatus.SC_OK) {
                val list = getServerResponse(
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
