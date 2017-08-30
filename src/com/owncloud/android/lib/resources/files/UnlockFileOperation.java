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
package com.owncloud.android.lib.resources.files;

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.DeleteMethod;


/**
 * Unlock a file
 */
public class UnlockFileOperation extends RemoteOperation {

    private static final String TAG = UnlockFileOperation.class.getSimpleName();
    private static final int SYNC_READ_TIMEOUT = 40000;
    private static final int SYNC_CONNECTION_TIMEOUT = 5000;
    private static final String LOCK_FILE_URL = "/ocs/v2.php/apps/end_to_end_encryption/api/v1/lock/";

    private String localId;
    private String token;

    /**
     * Constructor
     */
    public UnlockFileOperation(String localId, String token) {
        this.localId = localId;
        this.token = token;
    }

    /**
     * @param client Client object
     */
    @Override
    protected RemoteOperationResult run(OwnCloudClient client) {
        RemoteOperationResult result;
        DeleteMethod deleteMethod = null;

        try {
            // remote request
            deleteMethod = new DeleteMethod(client.getBaseUri() + LOCK_FILE_URL + localId);
            deleteMethod.addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE);
            deleteMethod.addRequestHeader("Content-Type", "application/x-www-form-urlencoded");

            NameValuePair[] deleteParams = new NameValuePair[1];
            deleteParams[0] = new NameValuePair("token", token);
            deleteMethod.setQueryString(deleteParams);

            int status = client.executeMethod(deleteMethod, SYNC_READ_TIMEOUT, SYNC_CONNECTION_TIMEOUT);

            if (status == HttpStatus.SC_OK) {
                String response = deleteMethod.getResponseBodyAsString();

                result = new RemoteOperationResult(true, deleteMethod);
            } else {
                result = new RemoteOperationResult(false, deleteMethod);
                client.exhaustResponse(deleteMethod.getResponseBodyAsStream());
            }
            deleteMethod.addRequestHeader("token", token);

            int status = client.executeMethod(deleteMethod, SYNC_READ_TIMEOUT, SYNC_CONNECTION_TIMEOUT);

            result = new RemoteOperationResult(status == HttpStatus.SC_OK, deleteMethod);
            
            client.exhaustResponse(deleteMethod.getResponseBodyAsStream());
        } catch (Exception e) {
            result = new RemoteOperationResult(e);
            e.printStackTrace();
            Log_OC.e(TAG, "Unlock file with id " + localId + " failed: " + result.getLogMessage(),
                    result.getException());
        } finally {
            if (deleteMethod != null)
                deleteMethod.releaseConnection();
        }
        return result;
    }
}
