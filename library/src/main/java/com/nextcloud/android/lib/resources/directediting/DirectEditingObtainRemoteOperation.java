/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2019-2024 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.android.lib.resources.directediting;

import com.google.gson.reflect.TypeToken;
import com.owncloud.android.lib.common.DirectEditing;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.ocs.ServerResponse;
import com.owncloud.android.lib.resources.OCSRemoteOperation;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 * Get all editor details from direct editing
 */

public class DirectEditingObtainRemoteOperation extends OCSRemoteOperation<DirectEditing> {
    private static final String TAG = DirectEditingObtainRemoteOperation.class.getSimpleName();
    private static final int SYNC_READ_TIMEOUT = 40000;
    private static final int SYNC_CONNECTION_TIMEOUT = 5000;
    private static final String DIRECT_ENDPOINT = "/ocs/v2.php/apps/files/api/v1/directEditing";

    protected RemoteOperationResult<DirectEditing> run(OwnCloudClient client) {
        RemoteOperationResult<DirectEditing> result;
        GetMethod getMethod = null;

        try {
            getMethod = new GetMethod(client.getBaseUri() + DIRECT_ENDPOINT + JSON_FORMAT);

            // remote request
            getMethod.addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE);

            int status = client.executeMethod(getMethod, SYNC_READ_TIMEOUT, SYNC_CONNECTION_TIMEOUT);

            if (status == HttpStatus.SC_OK) {
                DirectEditing directEditing = getServerResponse(getMethod,
                        new TypeToken<ServerResponse<DirectEditing>>() {
                        })
                        .getOcs().getData();

                result = new RemoteOperationResult<>(true, getMethod);
                result.setResultData(directEditing);
            } else {
                result = new RemoteOperationResult<>(false, getMethod);
                client.exhaustResponse(getMethod.getResponseBodyAsStream());
            }
        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG, "Get all direct editing informations failed: " + result.getLogMessage(),
                     result.getException());
        } finally {
            if (getMethod != null) {
                getMethod.releaseConnection();
            }
        }
        return result;
    }
}
