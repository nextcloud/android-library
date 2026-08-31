/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2015 ownCloud Inc.
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files;

import android.text.TextUtils;

import com.nextcloud.common.SessionTimeOut;
import com.nextcloud.common.SessionTimeOutKt;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.jackrabbit.webdav.client.methods.MkColMethod;


/**
 * Remote operation performing the creation of a new folder in the ownCloud server.
 *
 * @author David A. Velasco
 * @author masensio
 */
public class CreateFolderRemoteOperation extends RemoteOperation<String> {

    private static final String TAG = CreateFolderRemoteOperation.class.getSimpleName();


    private final boolean createFullPath;
    private final String remotePath;
    private final String token;
    private final SessionTimeOut sessionTimeOut;

    /**
     * Constructor
     *
     * @param remotePath     Full path to the new directory to create in the remote server.
     * @param createFullPath 'True' means that all the ancestor folders should be created
     *                       if don't exist yet.
     */
    public CreateFolderRemoteOperation(String remotePath, boolean createFullPath) {
        this(remotePath, createFullPath, SessionTimeOutKt.getDefaultSessionTimeOut());
    }

    public CreateFolderRemoteOperation(String remotePath, boolean createFullPath, String token) {
        this(remotePath, createFullPath, token, SessionTimeOutKt.getDefaultSessionTimeOut());
    }

    public CreateFolderRemoteOperation(String remotePath, boolean createFullPath, SessionTimeOut sessionTimeOut) {
        this(remotePath, createFullPath, "", sessionTimeOut);
    }

    public CreateFolderRemoteOperation(String remotePath, boolean createFullPath, String token, SessionTimeOut sessionTimeOut) {
        this.remotePath = remotePath;
        this.createFullPath = createFullPath;
        this.token = token;
        this.sessionTimeOut = sessionTimeOut;
    }

    /**
     * Performs the operation
     *
     * @param client Client object to communicate with the remote ownCloud server.
     */
    @Override
    protected RemoteOperationResult<String> run(OwnCloudClient client) {
        RemoteOperationResult<String> result;

        result = createFolder(client);
        if (!result.isSuccess() && createFullPath &&
                RemoteOperationResult.ResultCode.CONFLICT == result.getCode() &&
                !"/".equals(remotePath)) { // this must already exists
            result = createParentFolder(FileUtils.getParentPath(remotePath), client);
            if (result.isSuccess()) {
                result = createFolder(client);    // second (and last) try
            }
        }

        return result;
    }


    private RemoteOperationResult<String> createFolder(OwnCloudClient client) {
        RemoteOperationResult<String> result;
        MkColMethod mkCol = null;
        try {
            mkCol = new MkColMethod(client.getFilesDavUri(remotePath));

            if (!TextUtils.isEmpty(token)) {
                mkCol.addRequestHeader(E2E_TOKEN, token);
            }

            client.executeMethod(mkCol, sessionTimeOut.getReadTimeOut(), sessionTimeOut.getConnectionTimeOut());

            if (HttpStatus.SC_METHOD_NOT_ALLOWED == mkCol.getStatusCode()) {
                result = new RemoteOperationResult<>(RemoteOperationResult.ResultCode.FOLDER_ALREADY_EXISTS);
            } else {
                result = new RemoteOperationResult<>(mkCol.succeeded(), mkCol);
                Header fileIdHeader = mkCol.getResponseHeader("OC-FileId");

                if (fileIdHeader != null) {
                    String fileId = fileIdHeader.getValue();

                    result.setResultData(fileId);
                } else {
                    result.setResultData(null);
                }
            }

            Log_OC.d(TAG, "Create directory " + remotePath + ": " + result.getLogMessage());
            client.exhaustResponse(mkCol.getResponseBodyAsStream());
        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG, "Create directory " + remotePath + ": " + result.getLogMessage(), e);

        } finally {
            if (mkCol != null)
                mkCol.releaseConnection();
        }
        return result;
    }

    private RemoteOperationResult<String> createParentFolder(String parentPath, OwnCloudClient client) {
        RemoteOperation<String> operation = new CreateFolderRemoteOperation(parentPath, createFullPath);
        return operation.execute(client);
    }
}
