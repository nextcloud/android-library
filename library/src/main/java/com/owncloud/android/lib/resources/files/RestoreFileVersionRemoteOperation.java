/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2018-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2018 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files;

import android.net.Uri;
import android.util.Log;

import com.nextcloud.common.SessionTimeOut;
import com.nextcloud.common.SessionTimeOutKt;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.model.FileVersion;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.jackrabbit.webdav.client.methods.MoveMethod;

import java.io.IOException;


/**
 * Restore a {@link FileVersion}.
 */
public class RestoreFileVersionRemoteOperation extends RemoteOperation {

    private static final String TAG = RestoreFileVersionRemoteOperation.class.getSimpleName();

    private final long fileId;
    private final String fileName;
    private final SessionTimeOut sessionTimeOut;

    /**
     * Constructor
     *
     * @param fileId   fileId
     * @param fileName version date in unixtime
     */
    public RestoreFileVersionRemoteOperation(long fileId, String fileName) {
        this.fileId = fileId;
        this.fileName = fileName;
        sessionTimeOut = SessionTimeOutKt.getDefaultSessionTimeOut();
    }

    public RestoreFileVersionRemoteOperation(long fileId, String fileName, SessionTimeOut sessionTimeOut) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.sessionTimeOut = sessionTimeOut;
    }

    /**
     * Performs the operation.
     *
     * @param client Client object to communicate with the remote ownCloud server.
     */
    @Override
    protected RemoteOperationResult run(OwnCloudClient client) {

        MoveMethod move = null;
        RemoteOperationResult result;
        try {
            String source = client.getDavUri() + "/versions/" + client.getUserId() + "/versions/" + fileId + "/"
                    + Uri.encode(fileName);
            String target = client.getDavUri() + "/versions/" + client.getUserId() + "/restore/" + fileId;

            move = new MoveMethod(source, target, true);
            int status = client.executeMethod(move, sessionTimeOut.getReadTimeOut(), sessionTimeOut.getConnectionTimeOut());

            result = new RemoteOperationResult<>(isSuccess(status), move);

            client.exhaustResponse(move.getResponseBodyAsStream());
        } catch (IOException e) {
            result = new RemoteOperationResult<>(e);
            Log.e(TAG, "Restore file version with id " + fileId + " failed: " + result.getLogMessage(), e);
        } finally {
            if (move != null) {
                move.releaseConnection();
            }
        }

        return result;
    }

    private boolean isSuccess(int status) {
        return status == HttpStatus.SC_CREATED || status == HttpStatus.SC_NO_CONTENT;
    }
}
