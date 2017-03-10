/**
 * Nextcloud Android client application
 *
 * @author Mario Danic
 * Copyright (C) 2017 Mario Danic
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.owncloud.android.lib.common.utils;

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.network.WebdavEntry;
import com.owncloud.android.lib.resources.files.RemoteFile;

import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;

import java.util.ArrayList;

/**
 * WebDav helper.
 */

public class WebDavFileUtils {


    /**
     *  Read the data retrieved from the server about the contents of the target folder
     *
     *
     *  @param remoteData     	Full response got from the server with the data of the target
     *                          folder and its direct children.
     *  @param client           Client instance to the remote server where the data were
     *                          retrieved.
     *  @return
     */
    public ArrayList<Object> readData(MultiStatus remoteData, OwnCloudClient client, boolean isReadFolderOperation,
                                      boolean isSearchOperation, String username) {
        ArrayList<Object> mFolderAndFiles = new ArrayList<Object>();

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
        if (isSearchOperation && username != null) {
            stripString = stripString.substring(0, stripString.lastIndexOf("/")) + "/dav/files/" + username;
        }

        // loop to update every child
        RemoteFile remoteFile = null;
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
     * @param we        WebDAV entry read from the server for a WebDAV resource (remote file or folder).
     * @return          New OCFile instance representing the remote resource described by we.
     */
    private RemoteFile fillOCFile(WebdavEntry we) {
        RemoteFile file = new RemoteFile(we.decodedPath());
        file.setCreationTimestamp(we.createTimestamp());
        file.setLength(we.contentLength());
        file.setMimeType(we.contentType());
        file.setModifiedTimestamp(we.modifiedTimestamp());
        file.setEtag(we.etag());
        file.setPermissions(we.permissions());
        file.setRemoteId(we.remoteId());
        file.setSize(we.size());
        file.setQuotaUsedBytes(we.quotaUsedBytes());
        file.setQuotaAvailableBytes(we.quotaAvailableBytes());
        file.setFavorite(we.isFavorite());
        return file;
    }


}
