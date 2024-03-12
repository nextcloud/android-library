/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2021 Tobias Kaminsky <tobias@kaminsky.me>
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
 * Remote operation performing conversion of app password
 * f the client is authenticated with a valid app password true without password will returned.
 * If the client is authenticating with a real password an app password will be generated and returned.
 */
public class ConvertAppTokenRemoteOperation extends OCSRemoteOperation<String> {

    private static final String TAG = ConvertAppTokenRemoteOperation.class.getSimpleName();
    private static final String URL = "/ocs/v2.php/core/getapppassword";

    @Override
    public RemoteOperationResult<String> run(NextcloudClient client) {
        GetMethod method = null;
        RemoteOperationResult<String> result;

        try {
            // remote request
            method = new GetMethod(client.getBaseUri() + URL + JSON_FORMAT, true);

            int status = client.execute(method);

            if (status == HttpStatus.SC_OK) {
                ServerResponse<AppPassword> serverResponse = getServerResponse(method, new TypeToken<>() {});

                if (serverResponse != null) {
                    result = new RemoteOperationResult<>(true, method);
                    result.setResultData(serverResponse.getOcs().getData().getAppPassword());
                } else {
                    result = new RemoteOperationResult<>(false, method);
                }
            } else if (status == HttpStatus.SC_FORBIDDEN) {
                result = new RemoteOperationResult<>(true, method);
                result.setResultData("");
                method.releaseConnection();
            } else {
                result = new RemoteOperationResult<>(false, method);
                method.releaseConnection();
            }
        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG, "Conversion of app token failed: " + result.getLogMessage(), result.getException());
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
        }
        return result;
    }
}
