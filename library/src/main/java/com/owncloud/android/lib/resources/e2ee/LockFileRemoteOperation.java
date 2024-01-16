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

package com.owncloud.android.lib.resources.e2ee;

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.Utf8PostMethod;
import org.json.JSONObject;


/**
 * Lock a file
 */
public class LockFileRemoteOperation extends RemoteOperation<String> {

    private static final String TAG = LockFileRemoteOperation.class.getSimpleName();
    private static final int SYNC_READ_TIMEOUT = 40000;
    private static final int SYNC_CONNECTION_TIMEOUT = 5000;
    private static final String LOCK_FILE_URL = "/ocs/v2.php/apps/end_to_end_encryption/api/v1/lock/";

    private static final String COUNTER_HEADER = "X-NC-E2EE-COUNTER";

    private final long localId;
    private long counter = -1;

    // JSON node names
    private static final String NODE_OCS = "ocs";
    private static final String NODE_DATA = "data";

    /**
     * Constructor
     */
    public LockFileRemoteOperation(long localId, long counter) {
        this.localId = localId;
        this.counter = counter;
    }

    public LockFileRemoteOperation(long localId) {
        this.localId = localId;
    }

    /**
     * @param client Client object
     */
    @Override
    protected RemoteOperationResult<String> run(OwnCloudClient client) {
        RemoteOperationResult<String> result;
        Utf8PostMethod postMethod = null;

        try {
            postMethod = new Utf8PostMethod(client.getBaseUri() + LOCK_FILE_URL + localId + JSON_FORMAT);

            // remote request
            postMethod.addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE);
            postMethod.addRequestHeader(CONTENT_TYPE, FORM_URLENCODED);

            if (counter > 0) {
                postMethod.addRequestHeader(COUNTER_HEADER, String.valueOf(counter));
            }

            int status = client.executeMethod(postMethod, SYNC_READ_TIMEOUT, SYNC_CONNECTION_TIMEOUT);

            if (status == HttpStatus.SC_OK) {
                String response = postMethod.getResponseBodyAsString();

                // Parse the response
                JSONObject respJSON = new JSONObject(response);
                String token = respJSON
                        .getJSONObject(NODE_OCS)
                        .getJSONObject(NODE_DATA)
                        .getString(E2E_TOKEN);

                result = new RemoteOperationResult<>(true, postMethod);
                result.setResultData(token);
            } else {
                result = new RemoteOperationResult<>(false, postMethod);
                client.exhaustResponse(postMethod.getResponseBodyAsStream());
            }
        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG, "Lock file with id " + localId + " failed: " + result.getLogMessage(), result.getException());
        } finally {
            if (postMethod != null) {
                postMethod.releaseConnection();
            }
        }
        return result;
    }
}
