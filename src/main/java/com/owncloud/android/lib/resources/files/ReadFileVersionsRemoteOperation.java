/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2018 Tobias Kaminsky
 *   Copyright (C) 2018 Nextcloud GmbH
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

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.network.WebdavEntry;
import com.owncloud.android.lib.common.network.WebdavUtils;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.resources.files.model.FileVersion;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;

import java.util.ArrayList;

import androidx.annotation.NonNull;

/**
 * Remote operation performing the read of remote trashbin folder on Nextcloud server.
 */

public class ReadFileVersionsRemoteOperation extends RemoteOperation {

    private static final String TAG = ReadFileVersionsRemoteOperation.class.getSimpleName();

    private String fileId;
    private ArrayList<Object> versions;

    /**
     * Constructor
     *
     * @param fileId FileId of the file.
     */
    public ReadFileVersionsRemoteOperation(@NonNull String fileId) {
        this.fileId = fileId;
    }

    /**
     * Performs the read operation.
     *
     * @param client Client object to communicate with the remote ownCloud server.
     */
    @Override
    protected RemoteOperationResult run(OwnCloudClient client) {
        RemoteOperationResult result = null;
        PropFindMethod query = null;

        try {
            String uri = client.getNewWebdavUri() + "/versions/" + client.getUserId() + "/versions/" + fileId;
            DavPropertyNameSet propSet = WebdavUtils.getFileVersionPropSet();

            query = new PropFindMethod(uri, propSet, DavConstants.DEPTH_1);
            int status = client.executeMethod(query);

            // check and process response
            boolean isSuccess = (status == HttpStatus.SC_MULTI_STATUS || status == HttpStatus.SC_OK);

            if (isSuccess) {
                // get data from remote folder
                MultiStatus dataInServer = query.getResponseBodyAsMultiStatus();
                readData(dataInServer, client);

                // Result of the operation
                result = new RemoteOperationResult(true, query);
                // Add data to the result
                if (result.isSuccess()) {
                    result.setData(versions);
                }
            } else {
                // synchronization failed
                client.exhaustResponse(query.getResponseBodyAsStream());
                result = new RemoteOperationResult(false, query);
            }
        } catch (Exception e) {
            result = new RemoteOperationResult(e);
        } finally {
            if (query != null)
                query.releaseConnection();  // let the connection available for other methods

            if (result == null) {
                result = new RemoteOperationResult(new Exception("unknown error"));
                Log_OC.e(TAG, "Synchronized file with id " + fileId + ": failed");
            } else {
                if (result.isSuccess()) {
                    Log_OC.i(TAG, "Synchronized file with id " + fileId + ": " + result.getLogMessage());
                } else {
                    if (result.isException()) {
                        Log_OC.e(TAG, "Synchronized with id " + fileId + ": " + result.getLogMessage(),
                                result.getException());
                    } else {
                        Log_OC.w(TAG, "Synchronized with id " + fileId + ": " + result.getLogMessage());
                    }
                }
            }
        }

        return result;
    }

    /**
     * Read the data retrieved from the server about the file versions.
     *
     * @param remoteData Full response got from the server with the version data.
     * @param client     Client instance to the remote server where the data were retrieved.
     */
    private void readData(MultiStatus remoteData, OwnCloudClient client) {
        versions = new ArrayList<>();

        // parse data from remote folder
        String splitElement = client.getNewWebdavUri().getPath();

        // loop to update every child
        for (int i = 1; i < remoteData.getResponses().length; ++i) {
            versions.add(new FileVersion(fileId, new WebdavEntry(remoteData.getResponses()[i], splitElement)));
        }
    }
}
