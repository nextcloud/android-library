/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2019-2020 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2015 ownCloud Inc.
 * SPDX-FileCopyrightText: 2014 David A. Velasco <dvelasco@solidgear.es>
 * SPDX-FileCopyrightText: 2014 masensio <masensio@solidgear.es>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.shares;

import com.nextcloud.common.NextcloudClient;
import com.nextcloud.operations.DeleteMethod;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.apache.commons.httpclient.HttpStatus;

import java.util.List;

/**
 * Remove a share
 */

public class RemoveShareRemoteOperation extends RemoteOperation<List<OCShare>> {

    private static final String TAG = RemoveShareRemoteOperation.class.getSimpleName();

    private final long remoteShareId;

    /**
     * Constructor
     *
     * @param remoteShareId Share ID
     */

    public RemoveShareRemoteOperation(long remoteShareId) {
        this.remoteShareId = remoteShareId;

    }

    @Override
    protected RemoteOperationResult run(OwnCloudClient client) {
        RemoteOperationResult result;
        org.apache.jackrabbit.webdav.client.methods.DeleteMethod delete = null;

        try {
            delete = new org.apache.jackrabbit.webdav.client.methods.DeleteMethod(client.getBaseUri() + ShareUtils.SHARING_API_PATH + "/" + remoteShareId);

            delete.addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE);

            int status = client.executeMethod(delete);

            if (isSuccess(status)) {
                String response = delete.getResponseBodyAsString();

                // Parse xml response and obtain the list of shares
                ShareToRemoteOperationResultParser parser = new ShareToRemoteOperationResultParser(
                    new ShareXMLParser()
                );
                result = parser.parse(response);

                Log_OC.d(TAG, "Unshare " + remoteShareId + ": " + result.getLogMessage());

            } else {
                result = new RemoteOperationResult(false, delete);
            }
        } catch (Exception e) {
            result = new RemoteOperationResult(e);
            Log_OC.e(TAG, "Unshare Link Exception " + result.getLogMessage(), e);

        } finally {
            if (delete != null)
                delete.releaseConnection();
        }
        return result;
    }

    @Override
    public RemoteOperationResult<List<OCShare>> run(NextcloudClient client) {
        RemoteOperationResult<List<OCShare>> result;
        com.nextcloud.operations.DeleteMethod delete = null;

        try {
            delete = new DeleteMethod(client.getBaseUri() + ShareUtils.SHARING_API_PATH + "/" + remoteShareId, true);

            int status = client.execute(delete);

            if (isSuccess(status)) {
                String response = delete.getResponseBodyAsString();

                // Parse xml response and obtain the list of shares
                ShareToRemoteOperationResultParser parser = new ShareToRemoteOperationResultParser(
                    new ShareXMLParser()
                );
                result = parser.parse(response);

                Log_OC.d(TAG, "Unshare " + remoteShareId + ": " + result.getLogMessage());

            } else {
                result = new RemoteOperationResult<>(false, delete);
            }
        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG, "Unshare Link Exception " + result.getLogMessage(), e);

        } finally {
            if (delete != null)
                delete.releaseConnection();
        }
        return result;
    }


    private boolean isSuccess(int status) {
        return (status == HttpStatus.SC_OK);
    }
}
