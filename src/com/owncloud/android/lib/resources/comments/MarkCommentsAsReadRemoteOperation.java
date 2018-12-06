/*
 * Nextcloud Android client application
 *
 * @author Tobias Kaminsky
 * Copyright (C) 2018 Tobias Kaminsky
 * Copyright (C) 2018 Nextcloud GmbH.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
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

    private String fileId;

    public MarkCommentsAsReadRemoteOperation(String fileId) {
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

        String commentsPath = client.getNewWebdavUri() + COMMENTS_URL + fileId;

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
