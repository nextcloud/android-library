/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2017 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.e2ee;

import com.nextcloud.common.NextcloudClient;
import com.nextcloud.operations.DeleteMethod;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.apache.commons.httpclient.HttpStatus;


/**
 * Unlock a file
 */
public class UnlockFileRemoteOperation extends RemoteOperation<Void> {

    private static final String TAG = UnlockFileRemoteOperation.class.getSimpleName();
    private static final String LOCK_FILE_URL = "/ocs/v2.php/apps/end_to_end_encryption/api/v2/lock/";

    private final long localId;
    private final String token;

    /**
     * Constructor
     */
    public UnlockFileRemoteOperation(long localId, String token) {
        this.localId = localId;
        this.token = token;
    }

    /**
     * @param client Client object
     */
    @Override
    public RemoteOperationResult<Void> run(NextcloudClient client) {
        RemoteOperationResult<Void> result;
        com.nextcloud.operations.DeleteMethod deleteMethod = null;

        try {
            // remote request
            deleteMethod = new DeleteMethod(client.getBaseUri() + LOCK_FILE_URL + localId, true);
            deleteMethod.addRequestHeader(CONTENT_TYPE, FORM_URLENCODED);
            deleteMethod.addRequestHeader(E2E_TOKEN, token);

            int status = client.execute(deleteMethod);

            result = new RemoteOperationResult<>(status == HttpStatus.SC_OK, deleteMethod);
        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG, "Unlock file with id " + localId + " failed: " + result.getLogMessage(),
                     result.getException());
        } finally {
            if (deleteMethod != null)
                deleteMethod.releaseConnection();
        }
        return result;
    }
}
