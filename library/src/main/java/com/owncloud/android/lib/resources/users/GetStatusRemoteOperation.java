/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2020 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
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
 * Remote operation to get status
 */
public class GetStatusRemoteOperation extends OCSRemoteOperation<Status> {

    private static final String TAG = GetStatusRemoteOperation.class.getSimpleName();
    private static final String GET_STATUS_URL = "/ocs/v2.php/apps/user_status/api/v1/user_status";

    /**
     * @param client Client object
     */
    @Override
    public RemoteOperationResult<Status> run(NextcloudClient client) {
        GetMethod getMethod = null;
        RemoteOperationResult<Status> result;

        try {
            // remote request
            getMethod = new GetMethod(client.getBaseUri() + GET_STATUS_URL + JSON_FORMAT, true);

            int status = client.execute(getMethod);

            if (status == HttpStatus.SC_OK) {
                // Parse the response
                ServerResponse<Status> serverResponse = getServerResponse(getMethod,
                        new TypeToken<ServerResponse<Status>>() {
                        });

                result = new RemoteOperationResult<>(true, getMethod);
                result.setResultData(serverResponse.getOcs().getData());
            } else {
                // 404 if no status was set before
                if (HttpStatus.SC_NOT_FOUND == getMethod.getStatusCode()) {
                    result = new RemoteOperationResult<>(true, getMethod);
                    result.setResultData(new Status(StatusType.INVISIBLE, "", "", -1));
                } else {
                    result = new RemoteOperationResult<>(false, getMethod);
                    getMethod.releaseConnection();
                }
            }
        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG, "Fetching of own status failed: " + result.getLogMessage(), result.getException());
        } finally {
            if (getMethod != null) {
                getMethod.releaseConnection();
            }
        }
        return result;
    }
}
