/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2020-2024 Tobias Kaminsky
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.android.lib.resources.users;


import com.nextcloud.common.SessionTimeOut;
import com.nextcloud.common.SessionTimeOutKt;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.resources.OCSRemoteOperation;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONObject;

/**
 * Generate an app password via username / login password. Available since Nextcloud 15
 */


public class GenerateAppPasswordRemoteOperation extends OCSRemoteOperation<String> {
    private static final String TAG = GenerateAppPasswordRemoteOperation.class.getSimpleName();
    private static final String DIRECT_ENDPOINT = "/ocs/v2.php/core/getapppassword";

    // JSON node names
    private static final String NODE_OCS = "ocs";
    private static final String NODE_DATA = "data";
    private static final String NODE_APPPASSWORD = "apppassword";

    private final SessionTimeOut sessionTimeOut;

    public GenerateAppPasswordRemoteOperation() {
        this(SessionTimeOutKt.getDefaultSessionTimeOut());
    }

    public GenerateAppPasswordRemoteOperation(SessionTimeOut sessionTimeOut) {
        this.sessionTimeOut = sessionTimeOut;
    }

    protected RemoteOperationResult<String> run(OwnCloudClient client) {
        RemoteOperationResult<String> result;
        GetMethod getMethod = null;

        try {
            getMethod = new GetMethod(client.getBaseUri() + DIRECT_ENDPOINT + JSON_FORMAT);

            // remote request
            getMethod.addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE);

            int status = client.executeMethod(getMethod, sessionTimeOut.getReadTimeOut(), sessionTimeOut.getConnectionTimeOut());

            if (status == HttpStatus.SC_OK) {
                String response = getMethod.getResponseBodyAsString();

                JSONObject respJSON = new JSONObject(response);
                String password = respJSON.getJSONObject(NODE_OCS).getJSONObject(NODE_DATA).getString(NODE_APPPASSWORD);

                result = new RemoteOperationResult<>(true, getMethod);
                result.setResultData(password);
            } else {
                result = new RemoteOperationResult<>(false, getMethod);
                client.exhaustResponse(getMethod.getResponseBodyAsStream());
            }
        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG, "Generate app password failed: " + result.getLogMessage(),
                    result.getException());
        } finally {
            if (getMethod != null) {
                getMethod.releaseConnection();
            }
        }
        return result;
    }
}
