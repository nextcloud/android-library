/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2018-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2018 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.comments;

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
 * Mark all comments for a file as read
 */
public class MarkCommentsAsReadRemoteOperation extends RemoteOperation {
    private static final String COMMENTS_URL = "/comments/files/";

    private final long fileId;

    public MarkCommentsAsReadRemoteOperation(long fileId) {
        this.fileId = fileId;
    }

    @Override
    protected RemoteOperationResult run(OwnCloudClient client) {
        RemoteOperationResult result;
        PropPatchMethod propPatchMethod = null;

        DavPropertySet newProps = new DavPropertySet();
        DavPropertyNameSet removeProperties = new DavPropertyNameSet();

        DefaultDavProperty<String> readMarkerProperty = new DefaultDavProperty<>("oc:readMarker", "",
                Namespace.getNamespace(WebdavEntry.NAMESPACE_OC));
        newProps.add(readMarkerProperty);

        String commentsPath = client.getCommentsUri(fileId);

        try {
            propPatchMethod = new PropPatchMethod(commentsPath, newProps, removeProperties);
            int status = client.executeMethod(propPatchMethod);

            boolean isSuccess = status == HttpStatus.SC_NO_CONTENT || status == HttpStatus.SC_OK ||
                    status == HttpStatus.SC_MULTI_STATUS;

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
