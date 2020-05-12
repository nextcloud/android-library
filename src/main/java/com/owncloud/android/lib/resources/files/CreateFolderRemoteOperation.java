/* ownCloud Android Library is available under MIT license
 *   Copyright (C) 2015 ownCloud Inc.
 *   
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *   
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *   
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
 *   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
 *   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS 
 *   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN 
 *   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
 *   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
 *
 */

package com.owncloud.android.lib.resources.files;

import android.text.TextUtils;

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.network.WebdavUtils;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.jackrabbit.webdav.client.methods.MkColMethod;


/**
 * Remote operation performing the creation of a new folder in the ownCloud server.
 *
 * @author David A. Velasco
 * @author masensio
 */
public class CreateFolderRemoteOperation extends RemoteOperation {

    private static final String TAG = CreateFolderRemoteOperation.class.getSimpleName();

    private static final int READ_TIMEOUT = 30000;
    private static final int CONNECTION_TIMEOUT = 5000;


    private boolean createFullPath;
    private String remotePath;
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
    protected RemoteOperationResult run(OwnCloudClient client) {
        RemoteOperationResult result;

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


    private RemoteOperationResult createFolder(OwnCloudClient client) {
        RemoteOperationResult result;
        MkColMethod mkCol = null;
        try {
            mkCol = new MkColMethod(client.getWebdavUri() + WebdavUtils.encodePath(remotePath));

            if (!TextUtils.isEmpty(token)) {
                mkCol.addRequestHeader(E2E_TOKEN, token);
            }
            
            client.executeMethod(mkCol, READ_TIMEOUT, CONNECTION_TIMEOUT);

            if (HttpStatus.SC_METHOD_NOT_ALLOWED == mkCol.getStatusCode()) {
                result = new RemoteOperationResult(RemoteOperationResult.ResultCode.FOLDER_ALREADY_EXISTS);
            } else {
                result = new RemoteOperationResult(mkCol.succeeded(), mkCol);
            }

            Log_OC.d(TAG, "Create directory " + remotePath + ": " + result.getLogMessage());
            client.exhaustResponse(mkCol.getResponseBodyAsStream());
        } catch (Exception e) {
            result = new RemoteOperationResult(e);
            Log_OC.e(TAG, "Create directory " + remotePath + ": " + result.getLogMessage(), e);

        } finally {
            if (mkCol != null)
                mkCol.releaseConnection();
        }
        return result;
    }

    private RemoteOperationResult createParentFolder(String parentPath, OwnCloudClient client) {
        RemoteOperation operation = new CreateFolderRemoteOperation(parentPath, createFullPath);
        return operation.execute(client);
    }


}
