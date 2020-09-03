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
    public ArrayList<Object> readData(MultiStatus remoteData,
                                      OwnCloudClient client,
                                      boolean isReadFolderOperation,
                                      boolean isSearchOperation,
                                      String userIdPlain) {
        ArrayList<Object> mFolderAndFiles = new ArrayList<>();

        WebdavEntry we;
        int start = 1;

        if (isReadFolderOperation) {
            we = new WebdavEntry(remoteData.getResponses()[0],
                                 client.getWebdavUri().getPath());
            mFolderAndFiles.add(fillOCFile(we));
        } else {
            start = 0;
        }

        String stripString = client.getWebdavUri().getPath();
        if (isSearchOperation && userIdPlain != null) {
            stripString = stripString.substring(0, stripString.lastIndexOf("/")) + "/dav/files/" + userIdPlain;
            stripString = stripString.replaceAll(" ", "%20");
        }

        // loop to update every child
        RemoteFile remoteFile;
        MultiStatusResponse[] responses = remoteData.getResponses();
        for (int i = start; i < responses.length; i++) {
            /// new OCFile instance with the data from the server
            we = new WebdavEntry(responses[i], stripString);
            remoteFile = fillOCFile(we);
            mFolderAndFiles.add(remoteFile);
        }

        return mFolderAndFiles;
    }

    /**
     * Creates and populates a new {@link RemoteFile} object with the data read from the server.
     *
     * @param we WebDAV entry read from the server for a WebDAV resource (remote file or folder).
     * @return New OCFile instance representing the remote resource described by we.
     */
    private RemoteFile fillOCFile(WebdavEntry we) {
        RemoteFile file = new RemoteFile(we.decodedPath());
        file.setCreationTimestamp(we.getCreateTimestamp());
        file.setLength(we.getContentLength());
        file.setMimeType(we.getContentType());
        file.setModifiedTimestamp(we.getModifiedTimestamp());
        file.setEtag(we.getETag());
        file.setPermissions(we.getPermissions());
        file.setRemoteId(we.getRemoteId());
        file.setSize(we.getSize());
        file.setFavorite(we.isFavorite());
        file.setHasPreview(we.isHasPreview());
        file.setSharees(we.getSharees());
        return file;
    }
}
