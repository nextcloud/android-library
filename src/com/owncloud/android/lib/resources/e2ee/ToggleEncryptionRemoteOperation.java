/*
 * Nextcloud Android client application
 *
 * @author Tobias Kaminsky
 * Copyright (C) 2017 Tobias Kaminsky
 * Copyright (C) 2017 Nextcloud GmbH.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.owncloud.android.lib.resources.e2ee;

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

public class ToggleEncryptionRemoteOperation extends RemoteOperation {

    private static final String TAG = ToggleEncryptionRemoteOperation.class.getSimpleName();
    private static final int SYNC_READ_TIMEOUT = 40000;
    private static final int SYNC_CONNECTION_TIMEOUT = 5000;
    private static final String ENCRYPTED_URL = "/ocs/v2.php/apps/end_to_end_encryption/api/v1/encrypted/";

    private String localId;
    private String remotePath;
    private boolean encryption;

    /**
     * Constructor
     */
    public ToggleEncryptionRemoteOperation(String localId, String remotePath, boolean encryption) {
        this.localId = localId;
        this.remotePath = remotePath;
        this.encryption = encryption;
    }

    /**
     * @param client Client object
     */
    @Override
    protected RemoteOperationResult run(OwnCloudClient client) {
        RemoteOperationResult result;
        HttpMethodBase method = null;

        ReadFolderRemoteOperation remoteFolderOperation = new ReadFolderRemoteOperation(remotePath);
        RemoteOperationResult remoteFolderOperationResult = remoteFolderOperation.execute(client);

        // Abort if not empty
        // Result has always the folder and maybe children, so size == 1 is ok
        if (remoteFolderOperationResult.isSuccess() && remoteFolderOperationResult.getData().size() > 1) {
            return new RemoteOperationResult(false, "Non empty", HttpStatus.SC_FORBIDDEN);
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

            int status = client.executeMethod(method, SYNC_READ_TIMEOUT, SYNC_CONNECTION_TIMEOUT);

            if (status == HttpStatus.SC_OK) {
                result = new RemoteOperationResult(true, method);
            } else {
                result = new RemoteOperationResult(false, method);
                client.exhaustResponse(method.getResponseBodyAsStream());
            }
        } catch (Exception e) {
            result = new RemoteOperationResult(e);
            Log_OC.e(TAG, "Setting encryption status of " + localId + " failed: " + result.getLogMessage(),
                    result.getException());
        } finally {
            if (method != null)
                method.releaseConnection();
        }
        return result;
    }
}
