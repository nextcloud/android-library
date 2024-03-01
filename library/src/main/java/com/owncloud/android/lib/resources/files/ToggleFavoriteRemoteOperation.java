/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2017 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files;

import android.net.Uri;

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

/**
 * Favorite or unfavorite a file.
 */
public class ToggleFavoriteRemoteOperation extends RemoteOperation {
    private boolean makeItFavorited;
    private String filePath;

    public ToggleFavoriteRemoteOperation(boolean makeItFavorited, String filePath) {
        this.makeItFavorited = makeItFavorited;
        this.filePath = filePath;
    }

    @Override
    protected RemoteOperationResult run(OwnCloudClient client) {
        RemoteOperationResult result = null;
        PropPatchMethod propPatchMethod = null;

        DavPropertySet newProps = new DavPropertySet();
        DavPropertyNameSet removeProperties = new DavPropertyNameSet();

        if (makeItFavorited) {
            DefaultDavProperty<String> favoriteProperty = new DefaultDavProperty<>("oc:favorite", "1",
                    Namespace.getNamespace(WebdavEntry.NAMESPACE_OC));
            newProps.add(favoriteProperty);
        } else {
            removeProperties.add("oc:favorite", Namespace.getNamespace(WebdavEntry.NAMESPACE_OC));
        }

        String webDavUrl = client.getDavUri().toString();
        String encodedPath = (client.getUserId() + Uri.encode(filePath)).replace("%2F", "/");
        String fullFilePath = webDavUrl + "/files/" + encodedPath;

        try {
            propPatchMethod = new PropPatchMethod(fullFilePath, newProps, removeProperties);
            int status = client.executeMethod(propPatchMethod);

            boolean isSuccess = (status == HttpStatus.SC_MULTI_STATUS || status == HttpStatus.SC_OK);

            if (isSuccess) {
                result = new RemoteOperationResult(true, status, propPatchMethod.getResponseHeaders());
            } else {
                client.exhaustResponse(propPatchMethod.getResponseBodyAsStream());
                result = new RemoteOperationResult(false, status, propPatchMethod.getResponseHeaders());
            }
        } catch (IOException e) {
            result = new RemoteOperationResult(e);
        }  finally {
            if (propPatchMethod != null) {
                propPatchMethod.releaseConnection();  // let the connection available for other methods
            }
        }

        return result;
    }
}
