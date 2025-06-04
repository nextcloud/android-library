/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 TSI-mc <surinder.kumar@t-systems.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package com.owncloud.android.lib.resources.albums;

import android.net.Uri;

import com.nextcloud.common.SessionTimeOut;
import com.nextcloud.common.SessionTimeOutKt;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.network.WebdavEntry;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.jackrabbit.webdav.client.methods.PropPatchMethod;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.xml.Namespace;

import java.io.IOException;

public class ToggleAlbumFavoriteRemoteOperation extends RemoteOperation {
    private boolean makeItFavorited;
    private String filePath;
    private final SessionTimeOut sessionTimeOut;

    public ToggleAlbumFavoriteRemoteOperation(boolean makeItFavorited, String filePath) {
        this(makeItFavorited, filePath, SessionTimeOutKt.getDefaultSessionTimeOut());
    }

    public ToggleAlbumFavoriteRemoteOperation(boolean makeItFavorited, String filePath, SessionTimeOut sessionTimeOut) {
        this.makeItFavorited = makeItFavorited;
        this.filePath = filePath;
        this.sessionTimeOut = sessionTimeOut;
    }

    protected RemoteOperationResult run(OwnCloudClient client) {
        RemoteOperationResult result;
        PropPatchMethod propPatchMethod = null;
        DavPropertySet newProps = new DavPropertySet();
        DavPropertyNameSet removeProperties = new DavPropertyNameSet();
        if (this.makeItFavorited) {
            DefaultDavProperty<String> favoriteProperty = new DefaultDavProperty("oc:favorite", "1", Namespace.getNamespace(WebdavEntry.NAMESPACE_OC));
            newProps.add(favoriteProperty);
        } else {
            removeProperties.add("oc:favorite", Namespace.getNamespace(WebdavEntry.NAMESPACE_OC));
        }

        String webDavUrl = client.getDavUri().toString() + "/photos/";
        String encodedPath = (client.getUserId() + Uri.encode(this.filePath)).replace("%2F", "/");
        String fullFilePath = webDavUrl + encodedPath;

        try {
            propPatchMethod = new PropPatchMethod(fullFilePath, newProps, removeProperties);
            int status = client.executeMethod(propPatchMethod, sessionTimeOut.getReadTimeOut(), sessionTimeOut.getConnectionTimeOut());
            boolean isSuccess = (status == HttpStatus.SC_MULTI_STATUS || status == HttpStatus.SC_OK);
            if (isSuccess) {
                result = new RemoteOperationResult(true, status, propPatchMethod.getResponseHeaders());
            } else {
                client.exhaustResponse(propPatchMethod.getResponseBodyAsStream());
                result = new RemoteOperationResult(false, status, propPatchMethod.getResponseHeaders());
            }
        } catch (IOException e) {
            result = new RemoteOperationResult(e);
        } finally {
            if (propPatchMethod != null) {
                propPatchMethod.releaseConnection();
            }

        }

        return result;
    }
}