/**
 * Nextcloud Android client application
 *
 * @author Mario Danic
 * Copyright (C) 2017 Mario Danic
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.owncloud.android.lib.resources.files;

import android.net.Uri;

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.network.WebdavEntry;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.jackrabbit.webdav.client.methods.PropPatchMethod;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.xml.Namespace;

import java.io.IOException;

/**
 * Favorite or unfavorite a file
 */

public class ToggleFavoriteOperation extends RemoteOperation {
    private boolean makeItFavorited;
    private String filePath;

    public ToggleFavoriteOperation(boolean makeItFavorited, String filePath) {
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
            DavProperty favoriteProperty = new DefaultDavProperty("oc:favorite", "1",
                    Namespace.getNamespace(WebdavEntry.NAMESPACE_OC));
            newProps.add(favoriteProperty);
        } else {
            removeProperties.add("oc:favorite", Namespace.getNamespace(WebdavEntry.NAMESPACE_OC));
        }

        String webDavUrl = client.getNewWebdavUri(false).toString();
        int pos = filePath.lastIndexOf('/') + 1;
        filePath = filePath.substring(0, pos) + Uri.encode(filePath.substring(pos));

        String fullFilePath = webDavUrl + "/files/" + client.getCredentials().getUsername() + filePath;

        try {
            propPatchMethod = new PropPatchMethod(fullFilePath,
                    newProps,
                    removeProperties);
            int status = client.executeMethod(propPatchMethod);

            boolean isSuccess = (
                    status == HttpStatus.SC_MULTI_STATUS ||
                            status == HttpStatus.SC_OK
            );

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
