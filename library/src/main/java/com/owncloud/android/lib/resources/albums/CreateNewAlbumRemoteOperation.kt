/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 TSI-mc <surinder.kumar@t-systems.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package com.owncloud.android.lib.resources.albums;

import com.nextcloud.common.SessionTimeOut;
import com.nextcloud.common.SessionTimeOutKt;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.network.WebdavUtils;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.apache.jackrabbit.webdav.client.methods.MkColMethod;

public class CreateNewAlbumRemoteOperation extends RemoteOperation<Void> {
    private static final String TAG = CreateNewAlbumRemoteOperation.class.getSimpleName();
    private final String newAlbumName;
    private final SessionTimeOut sessionTimeOut;

    public CreateNewAlbumRemoteOperation(String newAlbumName) {
        this(newAlbumName, SessionTimeOutKt.getDefaultSessionTimeOut());
    }

    public CreateNewAlbumRemoteOperation(String newAlbumName, SessionTimeOut sessionTimeOut) {
        this.newAlbumName = newAlbumName;
        this.sessionTimeOut = sessionTimeOut;
    }

    public String getNewAlbumName() {
        return newAlbumName;
    }

    /**
     * Performs the operation.
     *
     * @param client Client object to communicate with the remote ownCloud server.
     */
    @Override
    protected RemoteOperationResult<Void> run(OwnCloudClient client) {
        MkColMethod mkCol = null;
        RemoteOperationResult<Void> result;
        try {
            mkCol = new MkColMethod(client.getBaseUri() + "/remote.php/dav/photos/" + client.getUserId() + "/albums" + WebdavUtils.encodePath(newAlbumName));
            client.executeMethod(mkCol, sessionTimeOut.getReadTimeOut(), sessionTimeOut.getConnectionTimeOut());
            if (405 == mkCol.getStatusCode()) {
                result = new RemoteOperationResult<>(RemoteOperationResult.ResultCode.FOLDER_ALREADY_EXISTS);
            } else {
                result = new RemoteOperationResult<>(mkCol.succeeded(), mkCol);
                result.setResultData(null);
            }

            Log_OC.d(TAG, "Create album " + newAlbumName + ": " + result.getLogMessage());
            client.exhaustResponse(mkCol.getResponseBodyAsStream());
        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG, "Create album " + newAlbumName + ": " + result.getLogMessage(), e);
        } finally {
            if (mkCol != null) {
                mkCol.releaseConnection();
            }

        }

        return result;
    }

}
