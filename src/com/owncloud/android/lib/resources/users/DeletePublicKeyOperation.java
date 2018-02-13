/*
 * Nextcloud Android client application
 *
 * @author Tobias Kaminsky
 * Copyright (C) 2018 Tobias Kaminsky
 * Copyright (C) 2018 Nextcloud GmbH.
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
package com.owncloud.android.lib.resources.users;

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.DeleteMethod;


/**
 * Remote operation performing to delete the public key for an user
 */

public class DeletePublicKeyOperation extends RemoteOperation {

    private static final String TAG = DeletePublicKeyOperation.class.getSimpleName();
    private static final int SYNC_READ_TIMEOUT = 40000;
    private static final int SYNC_CONNECTION_TIMEOUT = 5000;
    private static final String PUBLIC_KEY_URL = "/ocs/v2.php/apps/end_to_end_encryption/api/v1/public-key";

    /**
     * @param client Client object
     */
    @Override
    protected RemoteOperationResult run(OwnCloudClient client) {
        DeleteMethod postMethod = null;
        RemoteOperationResult result;

        try {
            // remote request
            postMethod = new DeleteMethod(client.getBaseUri() + PUBLIC_KEY_URL);
            postMethod.addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE);

            int status = client.executeMethod(postMethod, SYNC_READ_TIMEOUT, SYNC_CONNECTION_TIMEOUT);

            result = new RemoteOperationResult(status == HttpStatus.SC_OK, postMethod);

            client.exhaustResponse(postMethod.getResponseBodyAsStream());
        } catch (Exception e) {
            result = new RemoteOperationResult(e);
            Log_OC.e(TAG, "Deletion of public key failed: " + result.getLogMessage(), result.getException());
        } finally {
            if (postMethod != null)
                postMethod.releaseConnection();
        }
        return result;
    }

}
