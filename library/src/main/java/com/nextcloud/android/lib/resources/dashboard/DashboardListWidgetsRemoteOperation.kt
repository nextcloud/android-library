/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2022 Tobias Kaminsky
 *   Copyright (C) 2022 Nextcloud GmbH
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 *   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 *   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
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
                val list =
                    getServerResponse(
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
