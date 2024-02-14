/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2019 Tobias Kaminsky
 *   Copyright (C) 2019 Nextcloud GmbH
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

package com.owncloud.android.lib.resources.notifications;

import com.nextcloud.common.NextcloudClient;
import com.nextcloud.operations.DeleteMethod;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.apache.commons.httpclient.HttpStatus;

/**
 * Delete all notification, specified at 
 * {@link "https://github.com/nextcloud/notifications/blob/master/docs/ocs-endpoint-v2.md"}.
 */
public class DeleteAllNotificationsRemoteOperation extends RemoteOperation<Void> {

    // OCS Route
    private static final String OCS_ROUTE_LIST_V12_AND_UP =
            "/ocs/v2.php/apps/notifications/api/v2/notifications";

    @Override
    public RemoteOperationResult<Void> run(NextcloudClient client) {
        RemoteOperationResult<Void> result;
        int status;
        DeleteMethod delete = null;
        String url = client.getBaseUri() + OCS_ROUTE_LIST_V12_AND_UP;

        try {
            delete = new com.nextcloud.operations.DeleteMethod(url, true);

            status = client.execute(delete);
            String response = delete.getResponseBodyAsString();

            if (status == HttpStatus.SC_OK) {
                result = new RemoteOperationResult<>(true, delete);
                Log_OC.d(this, "Successful response: " + response);
            } else {
                result = new RemoteOperationResult<>(false, delete);
                Log_OC.e(this, "Failed response while getting user notifications ");
                Log_OC.e(this, "*** status code: " + status + " ;response message: " + response);
            }
        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(this, "Exception while getting remote notifications", e);
        } finally {
            if (delete != null) {
                delete.releaseConnection();
            }
        }

        return result;
    }
}
