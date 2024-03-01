/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2017 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.users;

import com.nextcloud.common.NextcloudClient;
import com.nextcloud.operations.GetMethod;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.apache.commons.httpclient.HttpStatus;
import org.json.JSONObject;

import java.util.HashMap;


/**
 * Remote operation performing the fetch of the public key for an user
 */

public class GetPublicKeyRemoteOperation extends RemoteOperation<String> {

    private static final String TAG = GetPublicKeyRemoteOperation.class.getSimpleName();
    private static final String PUBLIC_KEY_URL = "/ocs/v2.php/apps/end_to_end_encryption/api/v1/public-key";

    // JSON node names
    private static final String NODE_OCS = "ocs";
    private static final String NODE_DATA = "data";
    private static final String NODE_PUBLIC_KEYS = "public-keys";

    private String user;

    public GetPublicKeyRemoteOperation() {
        this.user = "";
    }

    public GetPublicKeyRemoteOperation(String user) {
        this.user = user;
    }

    /**
     * @param client Client object
     */
    @Override
    public RemoteOperationResult<String> run(NextcloudClient client) {
        GetMethod getMethod = null;
        RemoteOperationResult<String> result;

        try {
            // remote request
            getMethod = new GetMethod(client.getBaseUri() + PUBLIC_KEY_URL + JSON_FORMAT, true);

            if (!user.isEmpty()) {
                HashMap<String, String> map = new HashMap<>();
                map.put("users", "[\"" + user + "\"]");
                getMethod.setQueryString(map);
            } else {
                user = client.getUserId();
            }

            int status = client.execute(getMethod);

            if (status == HttpStatus.SC_OK) {
                String response = getMethod.getResponseBodyAsString();

                // Parse the response
                JSONObject respJSON = new JSONObject(response);
                String key = respJSON
                        .getJSONObject(NODE_OCS)
                        .getJSONObject(NODE_DATA)
                        .getJSONObject(NODE_PUBLIC_KEYS)
                        .getString(user);

                result = new RemoteOperationResult<>(true, getMethod);
                result.setResultData(key);
            } else {
                result = new RemoteOperationResult<>(false, getMethod);
            }
        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG,
                     "Fetching of public key failed for user " + user + ": " + result.getLogMessage(),
                     result.getException());
        } finally {
            if (getMethod != null)
                getMethod.releaseConnection();
        }
        return result;
    }
}
