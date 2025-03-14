/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2018-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2018 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
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

/**
 * Remote operation performing the read of remote versions on Nextcloud server.
 */

public class ReadFileVersionsRemoteOperation extends RemoteOperation<ArrayList<FileVersion>> {

    private static final String TAG = ReadFileVersionsRemoteOperation.class.getSimpleName();

    private final long localId;
    private ArrayList<FileVersion> versions;

    /**
     * Constructor
     *
     * @param fileId FileId of the file.
     */
    public ReadFileVersionsRemoteOperation(long fileId) {
        this.localId = fileId;
    }

    /**
     * Performs the read operation.
     *
     * @param client Client object to communicate with the remote ownCloud server.
     */
    @Override
    protected RemoteOperationResult<ArrayList<FileVersion>> run(OwnCloudClient client) {
        RemoteOperationResult<ArrayList<FileVersion>> result = null;
        PropFindMethod query = null;

        try {
            String uri = client.getDavUri() + "/versions/" + client.getUserId() + "/versions/" + localId;
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
                result = new RemoteOperationResult<>(true, query);
                // Add data to the result
                if (result.isSuccess()) {
                    result.setResultData(versions);
                }
            } else {
                // synchronization failed
                client.exhaustResponse(query.getResponseBodyAsStream());
                result = new RemoteOperationResult<>(false, query);
            }
        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
        } finally {
            if (query != null)
                query.releaseConnection();  // let the connection available for other methods

            if (result == null) {
                result = new RemoteOperationResult<>(new Exception("unknown error"));
                Log_OC.e(TAG, "Read file version for " + localId + ": failed");
            } else {
                if (result.isSuccess()) {
                    Log_OC.i(TAG, "Read file version for " + localId + ": " + result.getLogMessage());
                } else {
                    if (result.isException()) {
                        Log_OC.e(TAG, "Read file version for " + localId + ": " + result.getLogMessage(),
                                result.getException());
                    } else {
                        Log_OC.w(TAG, "Read file version for " + localId + ": " + result.getLogMessage());
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
        String splitElement = client.getDavUri().getPath();

        // loop to update every child
        for (int i = 1; i < remoteData.getResponses().length; ++i) {
            versions.add(new FileVersion(
                    localId,
                    new WebdavEntry(remoteData.getResponses()[i], splitElement))
            );
        }
    }
}
