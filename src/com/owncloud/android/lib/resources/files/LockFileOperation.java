/*
 * Nextcloud Android client application
 *
 * @author Tobias Kaminsky
 * Copyright (C) 2017 Tobias Kaminsky
 * Copyright (C) 2017 Nextcloud GmbH.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.owncloud.android.lib.resources.files;

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * Lock a file
 */
public class LockFileOperation extends RemoteOperation {

    private static final String TAG = LockFileOperation.class.getSimpleName();
    private static final int SYNC_READ_TIMEOUT = 40000;
    private static final int SYNC_CONNECTION_TIMEOUT = 5000;
    private static final String LOCK_FILE_URL = "/ocs/v2.php/apps/end_to_end_encryption/api/v1/lock/";

    private String localId;
    private String token;

    // JSON node names
    private static final String NODE_OCS = "ocs";
    private static final String NODE_DATA = "data";
    private static final String NODE_TOKEN = "token";

    private static final String JSON_FORMAT = "?format=json";

    /**
     * Constructor
     */
    public LockFileOperation(String localId, String token) {
        this.localId = localId;
        this.token = token;
    }

    public LockFileOperation(String localId) {
        this.localId = localId;
        this.token = "";
    }

    /**
     * @param client Client object
     */
    @Override
    protected RemoteOperationResult run(OwnCloudClient client) {
        RemoteOperationResult result;
        PostMethod postMethod = null;

        try {
            postMethod = new PostMethod(client.getBaseUri() + LOCK_FILE_URL + localId + JSON_FORMAT);

            if (!token.isEmpty()) {
                postMethod.setParameter("token", token);
            }

            // remote request
            postMethod.addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE);
            postMethod.addRequestHeader("Content-Type", "application/x-www-form-urlencoded");

            int status = client.executeMethod(postMethod, SYNC_READ_TIMEOUT, SYNC_CONNECTION_TIMEOUT);

            if (status == HttpStatus.SC_OK) {
                String response = postMethod.getResponseBodyAsString();

                // Parse the response
                JSONObject respJSON = new JSONObject(response);
                String token = (String) respJSON.getJSONObject(NODE_OCS).getJSONObject(NODE_DATA)
                        .get(NODE_TOKEN);

                result = new RemoteOperationResult(true, postMethod);
                ArrayList<Object> tokenArray = new ArrayList<>();
                tokenArray.add(token);
                result.setData(tokenArray);
            } else {
                result = new RemoteOperationResult(false, postMethod);
                client.exhaustResponse(postMethod.getResponseBodyAsStream());
            }
        } catch (Exception e) {
            result = new RemoteOperationResult(e);
            e.printStackTrace();
            Log_OC.e(TAG, "Lock file with id " + localId + " failed: " + result.getLogMessage(),
                    result.getException());
        } finally {
            if (postMethod != null)
                postMethod.releaseConnection();
        }
        return result;
    }
}
