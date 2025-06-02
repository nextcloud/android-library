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

import org.apache.jackrabbit.webdav.client.methods.MoveMethod;

public class RenameAlbumRemoteOperation extends RemoteOperation {
    private static final String TAG = RenameAlbumRemoteOperation.class.getSimpleName();
    private final String mOldRemotePath;
    private final String mNewAlbumName;
    private final SessionTimeOut sessionTimeOut;

    public RenameAlbumRemoteOperation(String mOldRemotePath, String mNewAlbumName) {
        this(mOldRemotePath, mNewAlbumName, SessionTimeOutKt.getDefaultSessionTimeOut());
    }

    public RenameAlbumRemoteOperation(String mOldRemotePath, String mNewAlbumName, SessionTimeOut sessionTimeOut) {
        this.mOldRemotePath = mOldRemotePath;
        this.mNewAlbumName = mNewAlbumName;
        this.sessionTimeOut = sessionTimeOut;
    }

    public String getNewAlbumName() {
        return mNewAlbumName;
    }

    /**
     * Performs the operation.
     *
     * @param client Client object to communicate with the remote ownCloud server.
     */
    @Override
    protected RemoteOperationResult run(OwnCloudClient client) {
        RemoteOperationResult result = null;
        MoveMethod move = null;
        String url = client.getBaseUri() + "/remote.php/dav/photos/" + client.getUserId() + "/albums";
        try {
            if (!this.mNewAlbumName.equals(this.mOldRemotePath)) {
                move = new MoveMethod(url + WebdavUtils.encodePath(mOldRemotePath), url + WebdavUtils.encodePath(mNewAlbumName), true);
                client.executeMethod(move, sessionTimeOut.getReadTimeOut(), sessionTimeOut.getConnectionTimeOut());
                result = new RemoteOperationResult(move.succeeded(), move);
                Log_OC.i(TAG, "Rename " + this.mOldRemotePath + " to " + this.mNewAlbumName + ": " + result.getLogMessage());
                client.exhaustResponse(move.getResponseBodyAsStream());
                return result;
            }
        } catch (Exception e) {
            result = new RemoteOperationResult(e);
            Log_OC.e(TAG, "Rename " + this.mOldRemotePath + " to " + this.mNewAlbumName + ": " + result.getLogMessage(), e);
            return result;
        } finally {
            if (move != null) {
                move.releaseConnection();
            }
        }

        return result;
    }

}
