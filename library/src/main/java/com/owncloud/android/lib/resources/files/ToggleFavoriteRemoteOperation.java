/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2017 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files;

import android.net.Uri;

import com.nextcloud.common.DavResponse;
import com.nextcloud.common.NextcloudClient;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.network.WebdavUtils;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.webdav.NCFavorite;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.jackrabbit.webdav.client.methods.PropPatchMethod;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.xml.Namespace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.bitfire.dav4jvm.Property;
import okhttp3.HttpUrl;

/**
 * Favorite or unfavorite a file.
 */
public class ToggleFavoriteRemoteOperation extends RemoteOperation<Boolean> {
    private final boolean makeItFavorited;
    private final String filePath;

    public ToggleFavoriteRemoteOperation(boolean makeItFavorited, String filePath) {
        this.makeItFavorited = makeItFavorited;
        this.filePath = filePath;
    }

    @Override
    protected RemoteOperationResult<Boolean> run(OwnCloudClient client) {
        RemoteOperationResult<Boolean> result;
        PropPatchMethod propPatchMethod = null;

        DavPropertySet newProps = new DavPropertySet();
        DavPropertyNameSet removeProperties = new DavPropertyNameSet();

        if (makeItFavorited) {
            DefaultDavProperty<String> favoriteProperty = new DefaultDavProperty<>("oc:favorite", "1",
                    Namespace.getNamespace(WebdavUtils.NAMESPACE_OC));
            newProps.add(favoriteProperty);
        } else {
            removeProperties.add("oc:favorite", Namespace.getNamespace(WebdavUtils.NAMESPACE_OC));
        }

        String webDavUrl = client.getDavUri().toString();
        String encodedPath = (client.getUserId() + Uri.encode(filePath)).replace("%2F", "/");
        String fullFilePath = webDavUrl + "/files/" + encodedPath;

        try {
            propPatchMethod = new PropPatchMethod(fullFilePath, newProps, removeProperties);
            int status = client.executeMethod(propPatchMethod);

            boolean isSuccess = (status == HttpStatus.SC_MULTI_STATUS || status == HttpStatus.SC_OK);

            if (isSuccess) {
                result = new RemoteOperationResult<>(true, status, propPatchMethod.getResponseHeaders());
            } else {
                client.exhaustResponse(propPatchMethod.getResponseBodyAsStream());
                result = new RemoteOperationResult<>(false, status, propPatchMethod.getResponseHeaders());
            }
        } catch (IOException e) {
            result = new RemoteOperationResult<>(e);
        } finally {
            if (propPatchMethod != null) {
                propPatchMethod.releaseConnection();  // let the connection available for other methods
            }
        }

        return result;
    }

    @Override
    public RemoteOperationResult<Boolean> run(NextcloudClient client) {
        RemoteOperationResult<Boolean> result;

        Map<Property.Name, String> setProperties = new HashMap<>();
        List<Property.Name> removeProperties = new ArrayList<>();

        if (makeItFavorited) {
            setProperties.put(NCFavorite.NAME, "1");
        } else {
            removeProperties.add(NCFavorite.NAME);
        }

        HttpUrl url = HttpUrl.get(client.getFilesDavUri(filePath));

        com.nextcloud.operations.PropPatchMethod propPatchMethod = new com.nextcloud.operations.PropPatchMethod(url,
            setProperties, removeProperties);

        DavResponse response = client.execute(propPatchMethod);

        if (response.getSuccess()) {
            result = new RemoteOperationResult<>(RemoteOperationResult.ResultCode.OK);
        } else {
            result = new RemoteOperationResult<>(RemoteOperationResult.ResultCode.WRONG_SERVER_RESPONSE);
        }

        return result;
    }
}
