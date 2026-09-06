/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2019 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.users;

import com.google.gson.Gson;
import com.nextcloud.common.NextcloudClient;
import com.nextcloud.operations.PostMethod;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.apache.commons.httpclient.HttpStatus;
import org.json.JSONObject;

import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.RequestBody;


/**
 * Remote operation performing check if app token is scheduled for remote wipe
 */

public class CheckRemoteWipeRemoteOperation extends RemoteOperation<Void> {

    private static final String TAG = CheckRemoteWipeRemoteOperation.class.getSimpleName();
    private static final String REMOTE_WIPE_URL = "/index.php/core/wipe/check";

    // JSON node names
    private static final String WIPE = "wipe";

    private final String authToken;

    public CheckRemoteWipeRemoteOperation(String authToken) {
        this.authToken = authToken;
    }

    /**
     * @param client Client object
     */
    @Override
    public RemoteOperationResult<Void> run(NextcloudClient client) {
        PostMethod postMethod = null;
        RemoteOperationResult<Void> result;

        try {
            HashMap<String, String> map = new HashMap<>();
            map.put(REMOTE_WIPE_TOKEN, authToken);

            String jsonString = new Gson().toJson(map);

            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonString);
            postMethod = new PostMethod(client.getBaseUri() + REMOTE_WIPE_URL + JSON_FORMAT, true, requestBody);
            postMethod.addRequestHeader(CONTENT_TYPE, FORM_URLENCODED);

            int status = client.execute(postMethod);

            if (HttpStatus.SC_OK == status) {
                String response = postMethod.getResponseBodyAsString();

                JSONObject json = new JSONObject(response);

                result = new RemoteOperationResult<>(json.getBoolean(WIPE), postMethod);
            } else {
                result = new RemoteOperationResult<>(false, postMethod);
            }
        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG,
                "Getting remote wipe status failed: " + result.getLogMessage(client.getContext()),
                result.getException());
        } finally {
            if (postMethod != null) {
                postMethod.releaseConnection();
            }
        }
        return result;
    }
}
