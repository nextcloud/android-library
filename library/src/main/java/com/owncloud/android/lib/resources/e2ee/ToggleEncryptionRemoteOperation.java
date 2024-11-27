/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2017 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.e2ee;

import com.nextcloud.common.SessionTimeOut;
import com.nextcloud.common.SessionTimeOutKt;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.resources.files.ReadFolderRemoteOperation;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.PutMethod;


/**
 * Set encryption of a folder
 */

public class ToggleEncryptionRemoteOperation extends RemoteOperation<Void> {

    private static final String TAG = ToggleEncryptionRemoteOperation.class.getSimpleName();
    private static final String ENCRYPTED_URL = "/ocs/v2.php/apps/end_to_end_encryption/api/v1/encrypted/";

    private final long localId;
    private final String remotePath;
    private final boolean encryption;

    private final SessionTimeOut sessionTimeOut;

    /**
     * Constructor
     */
    public ToggleEncryptionRemoteOperation(long localId, String remotePath, boolean encryption) {
        this.localId = localId;
        this.remotePath = remotePath;
        this.encryption = encryption;
        sessionTimeOut = SessionTimeOutKt.getDefaultSessionTimeOut();
    }

    public ToggleEncryptionRemoteOperation(long localId, String remotePath, boolean encryption, SessionTimeOut sessionTimeOut) {
        this.localId = localId;
        this.remotePath = remotePath;
        this.encryption = encryption;
        this.sessionTimeOut = sessionTimeOut;
    }

    /**
     * @param client Client object
     */
    @Override
    protected RemoteOperationResult<Void> run(OwnCloudClient client) {
        RemoteOperationResult<Void> result;
        HttpMethodBase method = null;

        ReadFolderRemoteOperation remoteFolderOperation = new ReadFolderRemoteOperation(remotePath);
        RemoteOperationResult remoteFolderOperationResult = remoteFolderOperation.execute(client);

        // Abort if not empty
        // Result has always the folder and maybe children, so size == 1 is ok
        if (remoteFolderOperationResult.isSuccess() && remoteFolderOperationResult.getData().size() > 1) {
            return new RemoteOperationResult<>(false, "Non empty", HttpStatus.SC_FORBIDDEN);
        }

        try {
            String url = client.getBaseUri() + ENCRYPTED_URL + localId;
            if (encryption) {
                method = new PutMethod(url);
            } else {
                method = new DeleteMethod(url);
            }

            // remote request
            method.addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE);
            method.addRequestHeader(CONTENT_TYPE, FORM_URLENCODED);

            int status = client.executeMethod(method, sessionTimeOut.getReadTimeOut(), sessionTimeOut.getConnectionTimeOut());

            if (status == HttpStatus.SC_OK) {
                result = new RemoteOperationResult<>(true, method);
            } else {
                result = new RemoteOperationResult<>(false, method);
                client.exhaustResponse(method.getResponseBodyAsStream());
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
