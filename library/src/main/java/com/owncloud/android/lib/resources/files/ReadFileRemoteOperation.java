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

import okhttp3.HttpUrl;


/**
 * Remote operation performing the read a file from the ownCloud server.
 *
 * @author David A. Velasco
 * @author masensio
 */

public class ReadFileRemoteOperation extends RemoteOperation<RemoteFile> {

    private static final String TAG = ReadFileRemoteOperation.class.getSimpleName();

    private final String mRemotePath;


    /**
     * Constructor
     *
     * @param remotePath Remote path of the file.
     */
    public ReadFileRemoteOperation(String remotePath) {
        mRemotePath = remotePath;
    }

    /**
     * Performs the read operation.
     *
     * @param client Client object to communicate with the remote ownCloud server.
     */
    @Override
    public RemoteOperationResult<RemoteFile> run(NextcloudClient client) {
        com.nextcloud.operations.PropFindMethod propFind;
        RemoteOperationResult<RemoteFile> result;

        try {
            // remote request
            HttpUrl url = HttpUrl.get(client.getFilesDavUri(mRemotePath));
            propFind = new com.nextcloud.operations.PropFindMethod(url, WebdavUtils.PROPERTYSETS.INSTANCE.getFILE(), 0);
            PropFindResult propFindResult = client.execute(propFind);

            result = new RemoteOperationResult<>(propFindResult.getDavResponse());
            result.setResultData(propFindResult.getRoot());

        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG, "Read file " + mRemotePath + " failed: " + result.getLogMessage(),
                result.getException());
        }

        return result;
    }

}
