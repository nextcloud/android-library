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
                val list = getServerResponse(
                    get,
                    object : TypeToken<ServerResponse<HashMap<String, DashboardWidget>>>() {}
                ).ocs.data

                result = RemoteOperationResult<Map<String, DashboardWidget>>(true, get)
                result.resultData = list
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
