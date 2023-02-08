/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2017 Tobias Kaminsky
 *   Copyright (C) 2017 Nextcloud GmbH
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
import com.nextcloud.operations.PostMethod;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.json.JSONObject;

import java.net.HttpURLConnection;

import okhttp3.FormBody;
import okhttp3.RequestBody;


/**
 * Remote operation performing the storage of the public key for an user
 */

public class SendCSRRemoteOperation extends RemoteOperation<String> {

    private static final String TAG = SendCSRRemoteOperation.class.getSimpleName();
    private static final String PUBLIC_KEY_URL = "/ocs/v2.php/apps/end_to_end_encryption/api/v1/public-key";
    private static final String CSR = "csr";

    // JSON node names
    private static final String NODE_OCS = "ocs";
    private static final String NODE_DATA = "data";
    private static final String NODE_PUBLIC_KEY = "public-key";

    private final String csr;

    /**
     * Constructor
     */
    public SendCSRRemoteOperation(String csr) {
        this.csr = csr;
    }

    /**
     * @param client Client object
     */
    @Override
    public RemoteOperationResult<String> run(NextcloudClient client) {
        PostMethod postMethod = null;
        RemoteOperationResult<String> result;

        try {
            // remote request
            RequestBody body = new FormBody
                    .Builder()
                    .add(CSR, csr)
                    .build();

            postMethod = new PostMethod(
                    client.getBaseUri() + PUBLIC_KEY_URL + JSON_FORMAT,
                    true,
                    body);

            int status = client.execute(postMethod);

            if (status == HttpURLConnection.HTTP_OK) {
                String response = postMethod.getResponseBodyAsString();

                // Parse the response
                JSONObject respJSON = new JSONObject(response);
                String key = (String) respJSON.getJSONObject(NODE_OCS).getJSONObject(NODE_DATA).get(NODE_PUBLIC_KEY);

                result = new RemoteOperationResult<>(true, postMethod);
                result.setResultData(key);
            } else {
                result = new RemoteOperationResult<>(false, postMethod);
            }

        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG, "Fetching of signing CSR failed: " + result.getLogMessage(), result.getException());
        } finally {
            if (postMethod != null) {
                postMethod.releaseConnection();
            }
        }
        return result;
    }

}
