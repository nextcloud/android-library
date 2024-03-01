/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2017 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.users;

import com.nextcloud.common.NextcloudClient;
import com.nextcloud.operations.PostMethod;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.json.JSONObject;

import okhttp3.FormBody;
import okhttp3.RequestBody;

/**
 * Remote operation performing the storage of the private key for an user
 */
public class StorePrivateKeyRemoteOperation extends RemoteOperation<String> {

    private static final String TAG = StorePrivateKeyRemoteOperation.class.getSimpleName();
    private static final String PRIVATE_KEY_URL = "/ocs/v2.php/apps/end_to_end_encryption/api/v1/private-key";
    private static final String PRIVATE_KEY = "privateKey";

    // JSON node names
    private static final String NODE_OCS = "ocs";
    private static final String NODE_DATA = "data";
    private static final String NODE_PRIVATE_KEY = "private-key";

    private final String privateKey;

    /**
     * Constructor
     */
    public StorePrivateKeyRemoteOperation(String privateKey) {
        this.privateKey = privateKey;
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
                    .add(PRIVATE_KEY, privateKey)
                    .build();

            postMethod = new PostMethod(client.getBaseUri() + PRIVATE_KEY_URL + JSON_FORMAT,
                                        true,
                                        body);

            client.execute(postMethod);

            if (postMethod.isSuccess()) {
                String response = postMethod.getResponseBodyAsString();

                // Parse the response
                JSONObject respJSON = new JSONObject(response);
                String key = (String) respJSON
                        .getJSONObject(NODE_OCS)
                        .getJSONObject(NODE_DATA)
                        .get(NODE_PRIVATE_KEY);

                result = new RemoteOperationResult<>(true, postMethod);
                result.setResultData(key);
            } else {
                result = new RemoteOperationResult<>(false, postMethod);
            }

        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG,
                     "Storing private key failed: " + result.getLogMessage(),
                     result.getException());
        } finally {
            if (postMethod != null)
                postMethod.releaseConnection();
        }
        return result;
    }

}
