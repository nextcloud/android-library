/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2017 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.e2ee;

import com.nextcloud.common.NextcloudClient;
import com.nextcloud.common.OkHttpMethodBase;
import com.nextcloud.operations.DeleteMethod;
import com.nextcloud.operations.PutMethod;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.resources.files.ReadFolderRemoteOperation;
import com.owncloud.android.lib.resources.files.model.RemoteFile;

import org.apache.commons.httpclient.HttpStatus;

import java.util.List;


/**
 * Set encryption of a folder
 */

public class ToggleEncryptionRemoteOperation extends RemoteOperation<Void> {

    private static final String TAG = ToggleEncryptionRemoteOperation.class.getSimpleName();
    private static final String ENCRYPTED_URL = "/ocs/v2.php/apps/end_to_end_encryption/api/v1/encrypted/";

    private final long localId;
    private final String remotePath;
    private final boolean encryption;

    /**
     * Constructor
     */
    public ToggleEncryptionRemoteOperation(long localId, String remotePath, boolean encryption) {
        this.localId = localId;
        this.remotePath = remotePath;
        this.encryption = encryption;
    }

    /**
     * @param client Client object
     */
    @Override
    public RemoteOperationResult<Void> run(NextcloudClient client) {
        RemoteOperationResult<Void> result;
        OkHttpMethodBase method = null;

        ReadFolderRemoteOperation remoteFolderOperation = new ReadFolderRemoteOperation(remotePath);
        RemoteOperationResult<List<RemoteFile>> remoteFolderOperationResult = remoteFolderOperation.execute(client);

        // Abort if not empty
        // Result has always the folder and maybe children, so size == 1 is ok
        if (remoteFolderOperationResult.isSuccess() && remoteFolderOperationResult.getResultData() != null && remoteFolderOperationResult.getResultData().size() > 1) {
            return new RemoteOperationResult<>(false, "Non empty", HttpStatus.SC_FORBIDDEN);
        }

        try {
            String url = client.getBaseUri() + ENCRYPTED_URL + localId;
            if (encryption) {
                method = new PutMethod(url, true, null);
            } else {
                method = new DeleteMethod(url, true);
            }

            // remote request
            method.addRequestHeader(CONTENT_TYPE, FORM_URLENCODED);

            int status = client.execute(method);

            if (status == HttpStatus.SC_OK) {
                result = new RemoteOperationResult<>(true, method);
            } else {
                result = new RemoteOperationResult<>(false, method);
            }
        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG, "Setting encryption status of " + localId + " failed: " + result.getLogMessage(),
                     result.getException());
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
        }
        return result;
    }
}
