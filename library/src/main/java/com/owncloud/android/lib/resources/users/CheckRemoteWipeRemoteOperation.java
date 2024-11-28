/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2019 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.users;

import com.nextcloud.common.SessionTimeOut;
import com.nextcloud.common.SessionTimeOutKt;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.Utf8PostMethod;
import org.json.JSONObject;


/**
 * Remote operation performing check if app token is scheduled for remote wipe
 */

public class CheckRemoteWipeRemoteOperation extends RemoteOperation {

    private static final String TAG = CheckRemoteWipeRemoteOperation.class.getSimpleName();
    private static final String REMOTE_WIPE_URL = "/index.php/core/wipe/check";

    // JSON node names
    private static final String WIPE = "wipe";
    private final SessionTimeOut sessionTimeOut;

    public CheckRemoteWipeRemoteOperation() {
        this(SessionTimeOutKt.getDefaultSessionTimeOut());
    }

    public CheckRemoteWipeRemoteOperation(SessionTimeOut sessionTimeOut) {
        this.sessionTimeOut = sessionTimeOut;
    }

    /**
     * @param client Client object
     */
    @Override
    protected RemoteOperationResult run(OwnCloudClient client) {
        Utf8PostMethod postMethod = null;
        RemoteOperationResult result;

        try {
            postMethod = new Utf8PostMethod(client.getBaseUri() + REMOTE_WIPE_URL + JSON_FORMAT);
            postMethod.addRequestHeader(CONTENT_TYPE, FORM_URLENCODED);
            postMethod.setParameter(REMOTE_WIPE_TOKEN, client.getCredentials().getAuthToken());

            int status = client.executeMethod(postMethod, sessionTimeOut.getReadTimeOut(), sessionTimeOut.getConnectionTimeOut());

            if (HttpStatus.SC_OK == status) {
                String response = postMethod.getResponseBodyAsString();

                JSONObject json = new JSONObject(response);

                if (json.getBoolean(WIPE)) {
                    result = new RemoteOperationResult<>(true, postMethod);
                } else {
                    result = new RemoteOperationResult<>(false, postMethod);
                }
            } else {
                result = new RemoteOperationResult<>(false, postMethod);
            }

            client.exhaustResponse(postMethod.getResponseBodyAsStream());
        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG,
                     "Getting remote wipe status failed: " + result.getLogMessage(),
                     result.getException());
        } finally {
            if (postMethod != null) {
                postMethod.releaseConnection();
            }
        }
        return result;
    }
}
