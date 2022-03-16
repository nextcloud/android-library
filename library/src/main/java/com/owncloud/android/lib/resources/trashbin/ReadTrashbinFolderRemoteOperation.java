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

package com.owncloud.android.lib.resources.trashbin;

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

/**
 * Remote operation performing the read of remote trashbin folder on Nextcloud server.
 */

public class ReadTrashbinFolderRemoteOperation extends RemoteOperation {

    private static final String TAG = ReadTrashbinFolderRemoteOperation.class.getSimpleName();
    
    private String remotePath;
    private ArrayList<Object> folderAndFiles;
    
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
    protected RemoteOperationResult run(OwnCloudClient client) {
        RemoteOperationResult result = null;
        PropFindMethod query = null;

        try {
            String baseUri = client.getDavUri() + "/trashbin/" + client.getUserId() + "/trash";
            DavPropertyNameSet propSet = WebdavUtils.getTrashbinPropSet();
                
            query = new PropFindMethod(baseUri + WebdavUtils.encodePath(remotePath), propSet, DavConstants.DEPTH_1);
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
                    result.setData(folderAndFiles);
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
