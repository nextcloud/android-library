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
package com.owncloud.android.lib.resources.users;

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * Remote operation performing the fetch of the public key for an user
 */

public class GetPublicKeyOperation extends RemoteOperation {

    private static final String TAG = GetPublicKeyOperation.class.getSimpleName();
    private static final int SYNC_READ_TIMEOUT = 40000;
    private static final int SYNC_CONNECTION_TIMEOUT = 5000;
    private static final String PUBLIC_KEY_URL = "/ocs/v2.php/apps/end_to_end_encryption/api/v1/public-key";

    // JSON node names
    private static final String NODE_OCS = "ocs";
    private static final String NODE_DATA = "data";
    private static final String NODE_PUBLIC_KEYS = "public-keys";

    private static final String JSON_FORMAT = "?format=json";

    /**
     * Constructor
     */
    public GetPublicKeyOperation() {
    }

    /**
     * @param client Client object
     */
    @Override
    protected RemoteOperationResult run(OwnCloudClient client) {
        GetMethod getMethod = null;
        RemoteOperationResult result;

        try {
            // remote request
            getMethod = new GetMethod(client.getBaseUri() + PUBLIC_KEY_URL + JSON_FORMAT);
            getMethod.addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE);

            int status = client.executeMethod(getMethod, SYNC_READ_TIMEOUT, SYNC_CONNECTION_TIMEOUT);

            if (status == HttpStatus.SC_OK) {
                String response = getMethod.getResponseBodyAsString();

                // Parse the response
                String username = client.getCredentials().getUsername();
                JSONObject respJSON = new JSONObject(response);
                String key = (String) respJSON.getJSONObject(NODE_OCS).getJSONObject(NODE_DATA)
                        .getJSONObject(NODE_PUBLIC_KEYS).get(username);

                result = new RemoteOperationResult(true, getMethod);
                ArrayList<Object> keys = new ArrayList<>();
                keys.add(key);
                result.setData(keys);
            } else {
                result = new RemoteOperationResult(false, getMethod);
                client.exhaustResponse(getMethod.getResponseBodyAsStream());
            }
        } catch (Exception e) {
            result = new RemoteOperationResult(e);
            e.printStackTrace();
            Log_OC.e(TAG, "Fetching of public key failed: " + result.getLogMessage(), result.getException());
        } finally {
            if (getMethod != null)
                getMethod.releaseConnection();
        }
        return result;
    }

}
