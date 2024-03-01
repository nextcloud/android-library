/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2017 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.users;

import com.nextcloud.common.NextcloudClient;
import com.nextcloud.operations.DeleteMethod;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import java.net.HttpURLConnection;


/**
 * Remote operation performing to delete the public key for an user
 */

public class DeletePublicKeyRemoteOperation extends RemoteOperation<Void> {

    private static final String TAG = DeletePublicKeyRemoteOperation.class.getSimpleName();
    private static final String PUBLIC_KEY_URL = "/ocs/v2.php/apps/end_to_end_encryption/api/v1/public-key";

    /**
     * @param client Client object
     */
    @Override
    public RemoteOperationResult<Void> run(NextcloudClient client) {
        DeleteMethod postMethod = null;
        RemoteOperationResult<Void> result;

        try {
            // remote request
            postMethod = new DeleteMethod(client.getBaseUri() + PUBLIC_KEY_URL, true);
            postMethod.addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE);

            int status = client.execute(postMethod);

            result = new RemoteOperationResult<>(status == HttpURLConnection.HTTP_OK, postMethod);
        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG, "Deletion of public key failed: " + result.getLogMessage(), result.getException());
        } finally {
            if (postMethod != null)
                postMethod.releaseConnection();
        }
        return result;
    }
}
