/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2015 ownCloud Inc.
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files;

import com.nextcloud.common.SessionTimeOut;
import com.nextcloud.common.SessionTimeOutKt;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.network.WebdavEntry;
import com.owncloud.android.lib.common.network.WebdavUtils;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.resources.files.model.RemoteFile;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;

import java.util.ArrayList;


/**
 * Remote operation performing the read a file from the ownCloud server.
 *
 * @author David A. Velasco
 * @author masensio
 */

public class ReadFileRemoteOperation extends RemoteOperation {

    private static final String TAG = ReadFileRemoteOperation.class.getSimpleName();
    private final String mRemotePath;
    private final SessionTimeOut sessionTimeOut;


    /**
     * Constructor
     *
     * @param remotePath Remote path of the file.
     */
    public ReadFileRemoteOperation(String remotePath) {
        mRemotePath = remotePath;
        sessionTimeOut = SessionTimeOutKt.getDefaultSessionTimeOut();
    }

    public ReadFileRemoteOperation(String remotePath, SessionTimeOut sessionTimeOut) {
        mRemotePath = remotePath;
        this.sessionTimeOut = sessionTimeOut;
    }

    /**
     * Performs the read operation.
     *
     * @param client Client object to communicate with the remote ownCloud server.
     */
    @Override
    protected RemoteOperationResult run(OwnCloudClient client) {
        PropFindMethod propfind = null;
        RemoteOperationResult result = null;

        /// take the duty of check the server for the current state of the file there
        try {
            // remote request
            propfind = new PropFindMethod(client.getFilesDavUri(mRemotePath),
                    WebdavUtils.getFilePropSet(),    // PropFind Properties
                    DavConstants.DEPTH_0);
            int status;
            status = client.executeMethod(propfind, sessionTimeOut.getReadTimeOut(), sessionTimeOut.getConnectionTimeOut());

            boolean isSuccess = (
                status == HttpStatus.SC_MULTI_STATUS ||
                    status == HttpStatus.SC_OK
            );
            if (isSuccess) {
                // Parse response
                MultiStatus resp = propfind.getResponseBodyAsMultiStatus();
                WebdavEntry we = new WebdavEntry(resp.getResponses()[0],
                        client.getFilesDavUri().getEncodedPath());
                RemoteFile remoteFile = new RemoteFile(we);
                ArrayList<Object> files = new ArrayList<Object>();
                files.add(remoteFile);

                // Result of the operation
                result = new RemoteOperationResult<>(true, propfind);
                result.setData(files);

            } else {
                result = new RemoteOperationResult<>(false, propfind);
                client.exhaustResponse(propfind.getResponseBodyAsStream());
            }

        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG, "Read file " + mRemotePath + " failed: " + result.getLogMessage(),
                result.getException());
        } finally {
            if (propfind != null)
                propfind.releaseConnection();
        }
        return result;
    }
}
