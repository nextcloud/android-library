/* Nextcloud Android Library is available under MIT license
 *
 *   @author Mario Danic
 *   Copyright (C) 2017 Mario Danic
 *   Copyright (C) 2017 Nextcloud GmbH
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

package com.owncloud.android.lib.common.utils;

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.network.WebdavEntry;
import com.owncloud.android.lib.resources.files.model.RemoteFile;

import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;

import java.util.ArrayList;

/**
 * WebDav helper.
 */
public class WebDavFileUtils {

    /**
     * Read the data retrieved from the server about the contents of the target folder
     *
     * @param remoteData Full response got from the server with the data of the target
     *                   folder and its direct children.
     * @param client     Client instance to the remote server where the data were
     *                   retrieved.
     * @return content of the target folder
     */
    public ArrayList<RemoteFile> readData(MultiStatus remoteData,
                                          OwnCloudClient client,
                                          boolean isReadFolderOperation,
                                          boolean isSearchOperation) {
        ArrayList<RemoteFile> mFolderAndFiles = new ArrayList<>();

        WebdavEntry we;
        int start = 1;

        if (isReadFolderOperation) {
            we = new WebdavEntry(remoteData.getResponses()[0],
                                 client.getFilesDavUri().getEncodedPath());
            mFolderAndFiles.add(new RemoteFile(we));
        } else {
            start = 0;
        }

        // loop to update every child
        RemoteFile remoteFile;
        MultiStatusResponse[] responses = remoteData.getResponses();
        for (int i = start; i < responses.length; i++) {
            /// new OCFile instance with the data from the server
            we = new WebdavEntry(responses[i], client.getFilesDavUri().getEncodedPath());
            remoteFile = new RemoteFile(we);
            mFolderAndFiles.add(remoteFile);
        }

        return mFolderAndFiles;
    }
}
