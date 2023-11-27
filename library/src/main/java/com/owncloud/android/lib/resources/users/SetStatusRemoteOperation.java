/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2020 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.users;

import com.nextcloud.common.JSONRequestBody;
import com.nextcloud.common.NextcloudClient;
import com.nextcloud.operations.PutMethod;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.resources.OCSRemoteOperation;

import org.apache.commons.httpclient.HttpStatus;

/**
 * Remote operation performing setting of status
 */
public class SetStatusRemoteOperation extends OCSRemoteOperation<Boolean> {

    private static final String TAG = SetStatusRemoteOperation.class.getSimpleName();
    private static final String SET_STATUS_URL = "/ocs/v2.php/apps/user_status/api/v1/user_status/status";

    private final StatusType type;

    public SetStatusRemoteOperation(StatusType type) {
        this.type = type;
    }

    /**
     * @param client Client object
     */
    @Override
    public RemoteOperationResult<Boolean> run(NextcloudClient client) {
        PutMethod putMethod = null;
        RemoteOperationResult<Boolean> result;

        try {
            // request body
            JSONRequestBody jsonRequestBody = new JSONRequestBody("statusType", type.toString());

            // remote request
            putMethod = new PutMethod(client.getBaseUri() + SET_STATUS_URL, true, jsonRequestBody.get());

            int status = client.execute(putMethod);

            if (status == HttpStatus.SC_OK) {
                result = new RemoteOperationResult<>(true, putMethod);
            } else {
                result = new RemoteOperationResult<>(false, putMethod);
                putMethod.releaseConnection();
            }
        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG, "Setting of own status failed: " + result.getLogMessage(), result.getException());
        } finally {
            if (putMethod != null) {
                putMethod.releaseConnection();
            }
        }
        return result;
    }
}
