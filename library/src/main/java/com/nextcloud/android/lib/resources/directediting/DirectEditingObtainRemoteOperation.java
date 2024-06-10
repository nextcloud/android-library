/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2019-2024 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.android.lib.resources.directediting;

import com.google.gson.reflect.TypeToken;
import com.nextcloud.common.NextcloudClient;
import com.nextcloud.operations.GetMethod;
import com.owncloud.android.lib.common.DirectEditing;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.ocs.ServerResponse;
import com.owncloud.android.lib.resources.OCSRemoteOperation;

import org.apache.commons.httpclient.HttpStatus;

/**
 * Get all editor details from direct editing
 */

public class DirectEditingObtainRemoteOperation extends OCSRemoteOperation<DirectEditing> {
    private static final String TAG = DirectEditingObtainRemoteOperation.class.getSimpleName();
    private static final String DIRECT_ENDPOINT = "/ocs/v2.php/apps/files/api/v1/directEditing";

    public RemoteOperationResult<DirectEditing> run(NextcloudClient client) {
        RemoteOperationResult<DirectEditing> result;
        GetMethod getMethod = null;

        try {
            getMethod = new GetMethod(client.getBaseUri() + DIRECT_ENDPOINT + JSON_FORMAT, true);

            int status = client.execute(getMethod);

            if (status == HttpStatus.SC_OK) {
                ServerResponse<DirectEditing> serverResponse = getServerResponse(getMethod,
                        new TypeToken<>() {
                        });

                if (serverResponse != null) {
                    DirectEditing directEditing = serverResponse.getOcs().getData();
                    result = new RemoteOperationResult<>(true, getMethod);
                    result.setResultData(directEditing);
                } else {
                    result = new RemoteOperationResult<>(false, getMethod);
                }

            } else {
                result = new RemoteOperationResult<>(false, getMethod);
            }
        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG, "Get all direct editing information failed: " + result.getLogMessage(),
                     result.getException());
        } finally {
            if (getMethod != null) {
                getMethod.releaseConnection();
            }
        }

        return result;
    }
}
