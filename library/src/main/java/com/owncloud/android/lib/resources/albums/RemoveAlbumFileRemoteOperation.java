/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 TSI-mc <surinder.kumar@t-systems.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package com.owncloud.android.lib.resources.albums;

import android.net.Uri;

import com.nextcloud.common.SessionTimeOut;
import com.nextcloud.common.SessionTimeOutKt;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.resources.files.RemoveFileRemoteOperation;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.jackrabbit.webdav.client.methods.DeleteMethod;

public class RemoveAlbumFileRemoteOperation extends RemoteOperation {
    private static final String TAG = RemoveFileRemoteOperation.class.getSimpleName();
    private final String mRemotePath;
    private final SessionTimeOut sessionTimeOut;

    public RemoveAlbumFileRemoteOperation(String remotePath) {
        this(remotePath, SessionTimeOutKt.getDefaultSessionTimeOut());
    }

    public RemoveAlbumFileRemoteOperation(String remotePath, SessionTimeOut sessionTimeOut) {
        this.mRemotePath = remotePath;
        this.sessionTimeOut = sessionTimeOut;
    }

    protected RemoteOperationResult run(OwnCloudClient client) {
        RemoteOperationResult result;
        DeleteMethod delete = null;
        String webDavUrl = client.getDavUri().toString()+"/photos/";
        String encodedPath = (client.getUserId() + Uri.encode(this.mRemotePath)).replace("%2F", "/");
        String fullFilePath = webDavUrl + encodedPath;

        try {
            delete = new DeleteMethod(fullFilePath);
            int status = client.executeMethod(delete, this.sessionTimeOut.getReadTimeOut(), this.sessionTimeOut.getConnectionTimeOut());
            delete.getResponseBodyAsString();
            result = new RemoteOperationResult(delete.succeeded() || status == HttpStatus.SC_NOT_FOUND, delete);
            Log_OC.i(TAG, "Remove " + this.mRemotePath + ": " + result.getLogMessage());
        } catch (Exception e) {
            result = new RemoteOperationResult(e);
            Log_OC.e(TAG, "Remove " + this.mRemotePath + ": " + result.getLogMessage(), e);
        } finally {
            if (delete != null) {
                delete.releaseConnection();
            }

        }

        return result;
    }
}
