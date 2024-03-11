/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2015 ownCloud Inc.
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files;

import android.text.TextUtils;

import com.nextcloud.common.DavResponse;
import com.nextcloud.common.NextcloudClient;
import com.nextcloud.operations.MkColMethod;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.apache.commons.httpclient.HttpStatus;

import okhttp3.HttpUrl;


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
    private String token;

    /**
     * Constructor
     *
     * @param remotePath     Full path to the new directory to create in the remote server.
     * @param createFullPath 'True' means that all the ancestor folders should be created
     *                       if don't exist yet.
     */
    public CreateFolderRemoteOperation(String remotePath, boolean createFullPath) {
        this.remotePath = remotePath;
        this.createFullPath = createFullPath;
    }

    public CreateFolderRemoteOperation(String remotePath, boolean createFullPath, String token) {
        this(remotePath, createFullPath);
        this.token = token;
    }

    /**
     * Performs the operation
     *
     * @param client Client object to communicate with the remote ownCloud server.
     */
    @Override
    public RemoteOperationResult<String> run(NextcloudClient client) {
        RemoteOperationResult<String> result;

        result = createFolder(client);
        if (!result.isSuccess() && createFullPath &&
                RemoteOperationResult.ResultCode.CONFLICT == result.getCode() &&
                !"/".equals(remotePath)) { // this must already exist
            result = createParentFolder(FileUtils.getParentPath(remotePath), client);
            if (result.isSuccess()) {
                result = createFolder(client);    // second (and last) try
            }
        }

        return result;
    }


    private RemoteOperationResult<String> createFolder(NextcloudClient client) {
        RemoteOperationResult<String> result;

        try {
            HttpUrl url = HttpUrl.get(client.getFilesDavUri(remotePath));
            com.nextcloud.operations.MkColMethod mkCol = new MkColMethod(url);

            if (!TextUtils.isEmpty(token)) {
                mkCol.addRequestHeader(E2E_TOKEN, token);
            }

            DavResponse response = client.execute(mkCol);

            if (HttpStatus.SC_METHOD_NOT_ALLOWED == response.getStatusCode()) {
                result = new RemoteOperationResult<>(RemoteOperationResult.ResultCode.FOLDER_ALREADY_EXISTS);
            } else {
                result = new RemoteOperationResult<>(response);
                String fileIdHeader = response.getHeader("OC-FileId");
                result.setResultData(fileIdHeader);
            }

            Log_OC.d(TAG, "Create directory " + remotePath + ": " + result.getLogMessage());
        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG, "Create directory " + remotePath + ": " + result.getLogMessage(), e);
        }

        return result;
    }

    private RemoteOperationResult<String> createParentFolder(String parentPath, NextcloudClient client) {
        RemoteOperation<String> operation = new CreateFolderRemoteOperation(parentPath, createFullPath);
        return operation.execute(client);
    }


}
