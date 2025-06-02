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

import org.apache.commons.httpclient.HttpStatus;
import org.apache.jackrabbit.webdav.client.methods.DeleteMethod;

public class RemoveAlbumRemoteOperation extends RemoteOperation {
    private static final String TAG = RemoveAlbumRemoteOperation.class.getSimpleName();
    private final String albumName;
    private final SessionTimeOut sessionTimeOut;

    public RemoveAlbumRemoteOperation(String albumName) {
        this(albumName, SessionTimeOutKt.getDefaultSessionTimeOut());
    }

    public RemoveAlbumRemoteOperation(String albumName, SessionTimeOut sessionTimeOut) {
        this.albumName = albumName;
        this.sessionTimeOut = sessionTimeOut;
    }

    /**
     * Performs the operation.
     *
     * @param client Client object to communicate with the remote ownCloud server.
     */
    @Override
    protected RemoteOperationResult run(OwnCloudClient client) {
        RemoteOperationResult result;
        DeleteMethod delete = null;

        try {
            delete = new DeleteMethod(client.getBaseUri() + "/remote.php/dav/photos/" + client.getUserId() + "/albums" + WebdavUtils.encodePath(albumName));
            int status = client.executeMethod(delete, sessionTimeOut.getReadTimeOut(), sessionTimeOut.getConnectionTimeOut());
            delete.getResponseBodyAsString();
            result = new RemoteOperationResult(delete.succeeded() || status == HttpStatus.SC_NOT_FOUND, delete);
            Log_OC.i(TAG, "Remove " + this.albumName + ": " + result.getLogMessage());
        } catch (Exception e) {
            result = new RemoteOperationResult(e);
            Log_OC.e(TAG, "Remove " + this.albumName + ": " + result.getLogMessage(), e);
        } finally {
            if (delete != null) {
                delete.releaseConnection();
            }

        }

        return result;
    }

}
