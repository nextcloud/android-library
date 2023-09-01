/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2017 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.common.utils;

import android.net.Uri;

import com.owncloud.android.lib.common.network.WebdavEntry;
import com.owncloud.android.lib.resources.files.model.RemoteFile;
import com.owncloud.android.lib.resources.files.webdav.NCEtag;
import com.owncloud.android.lib.resources.files.webdav.NCFavorite;
import com.owncloud.android.lib.resources.files.webdav.NCGetLastModified;
import com.owncloud.android.lib.resources.files.webdav.NCMountType;
import com.owncloud.android.lib.resources.files.webdav.NCPermissions;
import com.owncloud.android.lib.resources.files.webdav.NCRichWorkspace;
import com.owncloud.android.lib.resources.files.webdav.NCSharee;
import com.owncloud.android.lib.resources.files.webdav.NCTags;
import com.owncloud.android.lib.resources.files.webdav.OCId;
import com.owncloud.android.lib.resources.files.webdav.OCLocalId;
import com.owncloud.android.lib.resources.files.webdav.OCOwnerDisplayName;
import com.owncloud.android.lib.resources.files.webdav.OCOwnerId;
import com.owncloud.android.lib.resources.files.webdav.OCSize;

import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;

import java.util.ArrayList;
import java.util.List;

import at.bitfire.dav4jvm.Property;
import at.bitfire.dav4jvm.Response;
import at.bitfire.dav4jvm.property.GetContentType;
import at.bitfire.dav4jvm.property.ResourceType;
import okhttp3.MediaType;

/**
 * WebDav helper.
 */
public class WebDavFileUtils {

    /**
     * Read the data retrieved from the server about the contents of the target folder
     *
     * @param remoteData  Full response got from the server with the data of the target
     *                    folder and its direct children.
     * @param filesDavUri uri to files webdav uri
     * @return content of the target folder
     */
    public ArrayList<RemoteFile> readData(MultiStatus remoteData,
                                          Uri filesDavUri,
                                          boolean isReadFolderOperation,
                                          boolean isSearchOperation) {
        ArrayList<RemoteFile> mFolderAndFiles = new ArrayList<>();

        WebdavEntry we;
        int start = 1;

        if (isReadFolderOperation) {
            we = new WebdavEntry(remoteData.getResponses()[0],
                    filesDavUri.getEncodedPath());
            mFolderAndFiles.add(new RemoteFile(we));
        } else {
            start = 0;
        }

        // loop to update every child
        RemoteFile remoteFile;
        MultiStatusResponse[] responses = remoteData.getResponses();
        for (int i = start; i < responses.length; i++) {
            /// new OCFile instance with the data from the server
            we = new WebdavEntry(responses[i], filesDavUri.getEncodedPath());
            remoteFile = new RemoteFile(we);
            mFolderAndFiles.add(remoteFile);
        }

        return mFolderAndFiles;
    }

    public ArrayList<RemoteFile> readData(List<Response> responses, Uri filesDavUri) {
        ArrayList<RemoteFile> list = new ArrayList<>();

        for (Response response : responses) {
            list.add(parseResponse(response, filesDavUri));
        }

        return list;
    }

    public RemoteFile parseResponse(Response response, Uri filesDavUri) {
        RemoteFile remoteFile = new RemoteFile();
        String path = response.getHref().toString().split(filesDavUri.getEncodedPath(), 2)[1].replace("//", "/");

        for (Property property : response.getProperties()) {
            if (property instanceof NCEtag) {
                remoteFile.setEtag(((NCEtag) property).getEtag());
            }

            if (property instanceof NCFavorite) {
                remoteFile.setFavorite(((NCFavorite) property).isOcFavorite());
            }

            if (property instanceof NCGetLastModified) {
                remoteFile.setModifiedTimestamp(((NCGetLastModified) property).getLastModified());
            }

            if (property instanceof GetContentType) {
                MediaType type = ((GetContentType) property).getType();

                if (type != null) {
                    remoteFile.setMimeType(type.toString());
                } else {
                    remoteFile.setMimeType("");
                }
            }

            if (property instanceof ResourceType) {
                if (((ResourceType) property).getTypes().contains(ResourceType.Companion.getCOLLECTION())) {
                    remoteFile.setMimeType(WebdavEntry.DIR_TYPE);
                }
            }

            if (property instanceof NCPermissions) {
                remoteFile.setPermissions(((NCPermissions) property).getPermissions());
            }

            if (property instanceof OCId) {
                remoteFile.setRemoteId(((OCId) property).getOcId());
            }

            if (property instanceof OCSize) {
                remoteFile.setSize(((OCSize) property).getOcSize());
            }

            if (property instanceof OCLocalId) {
                remoteFile.setLocalId(((OCLocalId) property).getLocalId());
            }

            if (property instanceof NCMountType) {
                remoteFile.setMountType(((NCMountType) property).getType());
            }

            if (property instanceof OCOwnerId) {
                remoteFile.setOwnerId(((OCOwnerId) property).getOwnerId());
            }

            if (property instanceof OCOwnerDisplayName) {
                remoteFile.setOwnerDisplayName(((OCOwnerDisplayName) property).getString());
            }

            if (property instanceof NCRichWorkspace) {
                remoteFile.setRichWorkspace(((NCRichWorkspace) property).getRichWorkspace());
            }

            if (property instanceof NCSharee) {
                remoteFile.setSharees(((NCSharee) property).getSharees());
            }

            if (property instanceof NCTags) {
                remoteFile.setTags(((NCTags) property).getTags());
            }
        }

        remoteFile.setRemotePath(path);

        return remoteFile;
    }
}
