/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2017 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.users;

import com.google.gson.reflect.TypeToken;
import com.nextcloud.common.NextcloudClient;
import com.nextcloud.operations.GetMethod;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.ocs.ServerResponse;
import com.owncloud.android.lib.ocs.responses.PrivateKey;
import com.owncloud.android.lib.resources.OCSRemoteOperation;

import java.net.HttpURLConnection;

/**
 * Remote operation performing the fetch of the private key for an user
 */

public class GetPrivateKeyRemoteOperation extends OCSRemoteOperation<PrivateKey> {

    private static final String TAG = GetPrivateKeyRemoteOperation.class.getSimpleName();
    private static final String PRIVATE_KEY_URL = "/ocs/v2.php/apps/end_to_end_encryption/api/v1/private-key";

    /**
     * @param client Client object
     */
    @Override
    public RemoteOperationResult<PrivateKey> run(NextcloudClient client) {
        GetMethod getMethod = null;
        RemoteOperationResult<PrivateKey> result;

        try {
            // remote request
            getMethod = new GetMethod(client.getBaseUri() + PRIVATE_KEY_URL + JSON_FORMAT, true);

            int status = client.execute(getMethod);

            if (status == HttpURLConnection.HTTP_OK) {
                ServerResponse<PrivateKey> serverResponse =
                        getServerResponse(getMethod, new TypeToken<ServerResponse<PrivateKey>>() {
                        });

                if (serverResponse != null) {
                    result = new RemoteOperationResult<>(true, getMethod);
                    result.setResultData(serverResponse.getOcs().data);
                } else {
                    result = new RemoteOperationResult<>(false, getMethod);
                }
            } else {
                result = new RemoteOperationResult<>(false, getMethod);
            }

        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG, "Fetching of public key failed: " + result.getLogMessage(), result.getException());
        } finally {
            if (getMethod != null)
                getMethod.releaseConnection();
        }
        return result;
    }

}
