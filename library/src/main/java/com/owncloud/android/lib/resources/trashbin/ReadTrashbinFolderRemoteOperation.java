/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2018-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2018 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.trashbin;

import com.nextcloud.extensions.ArrayExtensionsKt;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.network.WebdavEntry;
import com.owncloud.android.lib.common.network.WebdavUtils;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.resources.trashbin.model.TrashbinFile;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Remote operation performing the read of remote trashbin folder on Nextcloud server.
 */
public class ReadTrashbinFolderRemoteOperation extends RemoteOperation<List<TrashbinFile>> {

    private static final String TAG = ReadTrashbinFolderRemoteOperation.class.getSimpleName();
    
    private final String remotePath;
    private ArrayList<TrashbinFile> folderAndFiles;
    
    /**
     * Constructor
     *
     * @param remotePath Remote path of the file.
     */
    public ReadTrashbinFolderRemoteOperation(String remotePath) {
        this.remotePath = remotePath;
    }

    /**
     * Performs the read operation.
     *
     * @param client Client object to communicate with the remote ownCloud server.
     */
    @Override
    public RemoteOperationResult<List<TrashbinFile>> run(OwnCloudClient client) {
        RemoteOperationResult<List<TrashbinFile>> result = null;
        PropFindMethod query = null;

        try {
            String baseUri = client.getDavUri() + "/trashbin/" + client.getUserId() + "/trash";
            DavPropertyNameSet propSet = ArrayExtensionsKt.toLegacyPropset(
                WebdavUtils.PROPERTYSETS.INSTANCE.getTRASHBIN()
            );
                
            query = new PropFindMethod(baseUri + WebdavUtils.INSTANCE.encodePath(remotePath), propSet, DavConstants.DEPTH_1);
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
                    result.setResultData(folderAndFiles);
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
                Log_OC.e(TAG, "Synchronized " + remotePath + ": failed");
            } else {
                if (result.isSuccess()) {
                    Log_OC.i(TAG, "Synchronized " + remotePath + ": " + result.getLogMessage());
                } else {
                    if (result.isException()) {
                        Log_OC.e(TAG, "Synchronized " + remotePath + ": " + result.getLogMessage(),
                                result.getException());
                    } else {
                        Log_OC.e(TAG, "Synchronized " + remotePath + ": " + result.getLogMessage());
                    }
                }
            }
        }
        
        return result;
    }

    /**
     * Read the data retrieved from the server about the contents of the target folder
     *
     * @param remoteData Full response got from the server with the data of the target
     *                   folder and its direct children.
     * @param client     Client instance to the remote server where the data were retrieved.
     */
    private void readData(MultiStatus remoteData, OwnCloudClient client) {
        folderAndFiles = new ArrayList<>();

        // parse data from remote folder
        WebdavEntry we;
        String splitElement = client.getDavUri().getPath();

        // loop to update every child
        for (int i = 1; i < remoteData.getResponses().length; ++i) {
            we = new WebdavEntry(remoteData.getResponses()[i], splitElement);
            folderAndFiles.add(new TrashbinFile(we, client.getUserId()));
        }
    }
}
