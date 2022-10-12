/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2021 Tobias Kaminsky
 *   Copyright (C) 2021 Nextcloud GmbH
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
import com.owncloud.android.lib.resources.OCSRemoteOperation;

import org.apache.commons.httpclient.HttpStatus;


/**
 * Remote operation performing conversion of app password
 * f the client is authenticated with a valid app password true without password will returned.
 * If the client is authenticating with a real password an app password will be generated and returned.
 */

public class ConvertAppTokenRemoteOperation extends OCSRemoteOperation<String> {

    private static final String TAG = ConvertAppTokenRemoteOperation.class.getSimpleName();
    private static final String URL = "/ocs/v2.php/core/getapppassword";

    @Override
    public RemoteOperationResult<String> run(NextcloudClient client) {
        GetMethod method = null;
        RemoteOperationResult<String> result;

        try {
            // remote request
            method = new GetMethod(client.getBaseUri() + URL + JSON_FORMAT, true);

            int status = client.execute(method);

            if (status == HttpStatus.SC_OK) {
                // Parse the response
                ServerResponse<AppPassword> serverResponse = getServerResponse(method,
                        new TypeToken<ServerResponse<AppPassword>>() {
                        });

                result = new RemoteOperationResult<>(true, method);
                result.setResultData(serverResponse.getOcs().getData().getAppPassword());
            } else if (status == HttpStatus.SC_FORBIDDEN) {
                result = new RemoteOperationResult<>(true, method);
                result.setResultData("");
                method.releaseConnection();
            } else {
                result = new RemoteOperationResult<>(false, method);
                method.releaseConnection();
            }
        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG, "Conversion of app token failed: " + result.getLogMessage(), result.getException());
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
        }
        return result;
    }
}
