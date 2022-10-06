/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2015 ownCloud Inc.
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files;

import com.nextcloud.common.NextcloudClient;
import com.nextcloud.operations.PropFindResult;
import com.owncloud.android.lib.common.network.WebdavUtils;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.resources.files.model.RemoteFile;

import java.util.List;

import okhttp3.HttpUrl;

/**
 * Remote operation performing the read of remote file or folder in the ownCloud server.
 *
 * @author David A. Velasco
 * @author masensio
 */

public class ReadFolderRemoteOperation extends RemoteOperation<List<RemoteFile>> {

    private static final String TAG = ReadFolderRemoteOperation.class.getSimpleName();

    private final String mRemotePath;

    /**
     * Constructor
     *
     * @param remotePath Remote path of the file.
     */
    public ReadFolderRemoteOperation(String remotePath) {
        mRemotePath = remotePath;
    }

    /**
     * Performs the read operation.
     *
     * @param client Client object to communicate with the remote ownCloud server.
     */
    @Override
    public RemoteOperationResult<List<RemoteFile>> run(NextcloudClient client) {
        RemoteOperationResult<List<RemoteFile>> result = null;
        com.nextcloud.operations.PropFindMethod propFind;

        try {
            // remote request
            HttpUrl url = HttpUrl.get(client.getFilesDavUri(mRemotePath));
            propFind = new com.nextcloud.operations.PropFindMethod(url, WebdavUtils.PROPERTYSETS.INSTANCE.getALL(), 1);
            PropFindResult propFindResult = client.execute(propFind);
            result = new RemoteOperationResult<>(propFindResult.getDavResponse());
            result.setResultData(propFindResult.getContent());
        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
        } finally {
            if (result == null) {
                result = new RemoteOperationResult<>(new Exception("unknown error"));
                Log_OC.e(TAG, "Synchronized " + mRemotePath + ": failed");
            } else {
                if (result.isSuccess()) {
                    Log_OC.i(TAG, "Synchronized " + mRemotePath + ": " + result.getLogMessage());
                } else {
                    if (result.isException()) {
                        Log_OC.e(TAG, "Synchronized " + mRemotePath + ": " + result.getLogMessage(),
                                result.getException());
                    } else {
                        Log_OC.e(TAG, "Synchronized " + mRemotePath + ": " + result.getLogMessage());
                    }
                }
            }
        }

        return result;
    }
}
