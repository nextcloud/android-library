/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2018-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2018 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.trashbin;

import android.net.Uri;
import android.util.Log;

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.network.WebdavUtils;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.trashbin.model.TrashbinFile;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.jackrabbit.webdav.client.methods.MoveMethod;

import java.io.IOException;

/**
 * Restore a {@link TrashbinFile}.
 */
public class RestoreTrashbinFileRemoteOperation extends RemoteOperation {

    private static final String TAG = RestoreTrashbinFileRemoteOperation.class.getSimpleName();
    private static final int RESTORE_READ_TIMEOUT = 30000;
    private static final int RESTORE_CONNECTION_TIMEOUT = 5000;

    private String sourcePath;
    private String fileName;

    /**
     * Constructor
     *
     * @param sourcePath Remote path of the {@link TrashbinFile} to restore
     * @param fileName   original filename
     */
    public RestoreTrashbinFileRemoteOperation(String sourcePath, String fileName) {
        this.sourcePath = sourcePath;
        this.fileName = fileName;
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
            String source = client.getDavUri() + WebdavUtils.encodePath(sourcePath);
            String target = client.getDavUri() + "/trashbin/" + client.getUserId() + "/restore/" +
                    Uri.encode(fileName);

            move = new MoveMethod(source, target, true);
            int status = client.executeMethod(move, RESTORE_READ_TIMEOUT, RESTORE_CONNECTION_TIMEOUT);

            result = new RemoteOperationResult(isSuccess(status), move);

            client.exhaustResponse(move.getResponseBodyAsStream());
        } catch (IOException e) {
            result = new RemoteOperationResult(e);
            Log.e(TAG, "Restore trashbin file " + sourcePath + " failed: " + result.getLogMessage(), e);
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
