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

import com.nextcloud.common.NextcloudClient;
import com.nextcloud.operations.PutMethod;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.resources.OCSNextcloudRemoteOperation;

import org.apache.commons.httpclient.HttpStatus;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * Remote operation performing setting of status
 */
public class SetStatusRemoteOperation extends OCSNextcloudRemoteOperation<Boolean> {

    private static final String TAG = SetStatusRemoteOperation.class.getSimpleName();
    private static final String SET_STATUS_URL = "/ocs/v2.php/apps/user_status/api/v1/user_status/status";

    private final StatusType type;

    public SetStatusRemoteOperation(StatusType type) {
        this.type = type;
    }

    /**
     * @param client Client object
     */
    @Override
    public RemoteOperationResult<Boolean> run(NextcloudClient client) {
        PutMethod putMethod = null;
        RemoteOperationResult<Boolean> result;

        try {
            // request body
            MediaType json = MediaType.parse("application/json; charset=utf-8");
            RequestBody requestBody = RequestBody.create(json, "{\"statusType\": \"" + type.getString() + "\"}");

            // remote request
            putMethod = new PutMethod(client.getBaseUri() + SET_STATUS_URL, true, requestBody);

            int status = client.execute(putMethod);

            if (status == HttpStatus.SC_OK) {
                result = new RemoteOperationResult<>(true, putMethod);
            } else {
                result = new RemoteOperationResult<>(false, putMethod);
                putMethod.releaseConnection();
            }
        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG, "Setting of own status failed: " + result.getLogMessage(), result.getException());
        } finally {
            if (putMethod != null) {
                putMethod.releaseConnection();
            }
        }
        return result;
    }
}
