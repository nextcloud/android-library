/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 TSI-mc <surinder.kumar@t-systems.com>
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2017 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.common.utils;

import android.net.Uri;

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

    /**
     * Read the data retrieved from the server about the contents of the target folder
     *
     * @param remoteData Full response got from the server with the data of the target
     *                   folder and its direct children.
     * @param client     Client instance to the remote server where the data were
     *                   retrieved.
     * @return
     */
    public ArrayList<RemoteFile> readAlbumData(MultiStatus remoteData, OwnCloudClient client) {
        String url = client.getBaseUri() + "/remote.php/dav/photos/" + client.getUserId();

        ArrayList<RemoteFile> mFolderAndFiles = new ArrayList<>();

        // loop to update every child
        // reading from 1 as 0th item will be just the root album path
        for (int i = 1; i < remoteData.getResponses().length; ++i) {
            /// new OCFile instance with the data from the server
            WebdavEntry we = new WebdavEntry(remoteData.getResponses()[i], Uri.parse(url).getEncodedPath());
            RemoteFile remoteFile = new RemoteFile(we);
            mFolderAndFiles.add(remoteFile);
        }
        return mFolderAndFiles;
    }
}
