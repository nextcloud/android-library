/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2017 Tobias Kaminsky
 *   Copyright (C) 2017 Nextcloud GmbH
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 *   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 *   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
 *
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
