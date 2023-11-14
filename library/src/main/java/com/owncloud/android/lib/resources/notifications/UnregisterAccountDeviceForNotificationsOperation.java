/*  Nextcloud Android Library is available under MIT license
 *   Copyright (C) 2017 Mario Danic
 *
 *   @author Mario Danic
 *
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

public class UnregisterAccountDeviceForNotificationsOperation extends RemoteOperation<Void> {

    // OCS Route
    private static final String OCS_ROUTE =
            "/ocs/v2.php/apps/notifications/api/v2/push";

    private static final String TAG = UnregisterAccountDeviceForNotificationsOperation.class.getSimpleName();

    @Override
    public RemoteOperationResult<Void> run(NextcloudClient client) {
        RemoteOperationResult<Void> result;
        int status;
        DeleteMethod delete = null;

        try {
            // Post Method
            delete = new com.nextcloud.operations.DeleteMethod(client.getBaseUri() + OCS_ROUTE, true);

            status = client.execute(delete);
            String response = delete.getResponseBodyAsString();

            if(status == HttpStatus.SC_OK) {
                result = new RemoteOperationResult<>(true, delete);
                Log_OC.d(TAG, "Successful response: " + response);
            } else {
                result = new RemoteOperationResult<>(false, delete);
            }

        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG, "Exception while registering device for notifications", e);

        } finally {
            if (delete != null) {
                delete.releaseConnection();
            }
        }
        return result;
    }
}
