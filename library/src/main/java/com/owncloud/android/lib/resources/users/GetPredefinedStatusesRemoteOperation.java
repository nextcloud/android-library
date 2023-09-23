/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2020 Tobias Kaminsky
 *   Copyright (C) 2020 Nextcloud GmbH
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

package com.owncloud.android.lib.resources.users;

import com.google.gson.reflect.TypeToken;
import com.nextcloud.common.NextcloudClient;
import com.nextcloud.operations.GetMethod;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.ocs.ServerResponse;
import com.owncloud.android.lib.resources.OCSNextcloudRemoteOperation;

import org.apache.commons.httpclient.HttpStatus;

import java.util.ArrayList;

/**
 * Remote operation to get all predefined statuses
 */
public class GetPredefinedStatusesRemoteOperation extends OCSNextcloudRemoteOperation<ArrayList<PredefinedStatus>> {
    private static final String TAG = GetPredefinedStatusesRemoteOperation.class.getSimpleName();
    private static final String GET_STATUS_URL = "/ocs/v2.php/apps/user_status/api/v1/predefined_statuses";

    /**
     * @param client Client object
     */
    @Override
    public RemoteOperationResult<ArrayList<PredefinedStatus>> run(NextcloudClient client) {
        GetMethod getMethod = null;
        RemoteOperationResult<ArrayList<PredefinedStatus>> result;

        try {
            // remote request
            getMethod = new GetMethod(client.getBaseUri() + GET_STATUS_URL + JSON_FORMAT, true);

            int status = client.execute(getMethod);

            if (status == HttpStatus.SC_OK) {
                // Parse the response
                ServerResponse<ArrayList<PredefinedStatus>> serverResponse =
                        getServerResponse(getMethod,
                                new TypeToken<ServerResponse<ArrayList<PredefinedStatus>>>() {
                                });

                result = new RemoteOperationResult<>(true, getMethod);
                result.setResultData(serverResponse.getOcs().getData());
            } else {
                result = new RemoteOperationResult<>(false, getMethod);
                getMethod.releaseConnection();
            }
        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG, "Fetching of predefined statuses failed: " + result.getLogMessage(), result.getException());
        } finally {
            if (getMethod != null) {
                getMethod.releaseConnection();
            }
        }
        return result;
    }
}
