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


/**
 * Remote operation notifying server for successful wipe
 */

public class RemoteWipeSuccessRemoteOperation extends RemoteOperation {

    private static final String TAG = RemoteWipeSuccessRemoteOperation.class.getSimpleName();
    private static final String REMOTE_WIPE_URL = "/index.php/core/wipe/success";

    private final String appToken;
    private final SessionTimeOut sessionTimeOut;

    public RemoteWipeSuccessRemoteOperation(String appToken) {
        this(appToken, SessionTimeOutKt.getDefaultSessionTimeOut());
    }

    public RemoteWipeSuccessRemoteOperation(String appToken, SessionTimeOut sessionTimeOut) {
        this.appToken = appToken;
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
            postMethod = new Utf8PostMethod(client.getBaseUri() + REMOTE_WIPE_URL);
            postMethod.addRequestHeader(CONTENT_TYPE, FORM_URLENCODED);
            postMethod.setParameter(REMOTE_WIPE_TOKEN, appToken);

            int status = client.executeMethod(postMethod, sessionTimeOut.getReadTimeOut(), sessionTimeOut.getConnectionTimeOut());
            client.exhaustResponse(postMethod.getResponseBodyAsStream());

            if (HttpStatus.SC_OK == status) {
                result = new RemoteOperationResult<>(RemoteOperationResult.ResultCode.OK);
            } else {
                result = new RemoteOperationResult<>(
                        RemoteOperationResult.ResultCode.valueOf(String.valueOf(status)));
            }
        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG,
                     "Setting status of remote wipe status failed: " + result.getLogMessage(),
                     result.getException());
        } finally {
            if (postMethod != null) {
                postMethod.releaseConnection();
            }
        }
        return result;
    }
}
