/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2021 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2015 ownCloud Inc.
 * SPDX-FileCopyrightText: 2015 David A. Velasco <dvelasco@solidgear.es>
 * SPDX-FileCopyrightText: 2014 masensio <masensio@solidgear.es>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.shares;

import android.text.TextUtils;

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.Utf8PostMethod;

import java.io.IOException;
import java.util.List;

/**
 * Creates a new share.  This allows sharing with a user or group or as a link.
 */
public class CreateShareRemoteOperation extends RemoteOperation<List<OCShare>> {

    private static final String TAG = CreateShareRemoteOperation.class.getSimpleName();

    private static final String PARAM_PATH = "path";
    private static final String PARAM_SHARE_TYPE = "shareType";
    private static final String PARAM_SHARE_WITH = "shareWith";
    private static final String PARAM_PUBLIC_UPLOAD = "publicUpload";
    private static final String PARAM_PASSWORD = "password";
    private static final String PARAM_PERMISSIONS = "permissions";
    private static final String PARAM_NOTE = "note";
    private static final String PARAM_ATTRIBUTES = "attributes";

    private final String remoteFilePath;
    private final ShareType shareType;
    private final String shareWith;
    private final boolean publicUpload;
    private final String password;
    private final int permissions;
    private boolean getShareDetails;
    private String note;
    private String attributes;

    /**
     * Constructor
     *
     * @param remoteFilePath Full path of the file/folder being shared. Mandatory argument
     * @param shareType      0 = user, 1 = group, 3 = Public link. Mandatory argument
     * @param shareWith      User/group ID with who the file should be shared.  This is mandatory for shareType
     *                       of 0 or 1
     * @param publicUpload   If false (default) public cannot upload to a public shared folder.
     *                       If true public can upload to a shared folder. Only available for public link shares
     * @param password       Password to protect a public link share. Only available for public link shares
     * @param permissions    1 - Read only Default for public shares
     * @param getShareDetails if true return share info
     *                       2 - Update
     *                       4 - Create
     *                       8 - Delete
     *                       16- Re-share
     *                       31- All above Default for private shares
     *                       For user or group shares.
     *                       To obtain combinations, add the desired values together.
     *                       For instance, for Re-Share, delete, read, update, add 16+8+2+1 = 27.
     * @param attributes     Share attributes are used for more advanced flags like permissions.
     */
    public CreateShareRemoteOperation(
            String remoteFilePath,
            ShareType shareType,
            String shareWith,
            boolean publicUpload,
            String password,
            int permissions,
            boolean getShareDetails,
            String note,
            String attributes
    ) {
        this.remoteFilePath = remoteFilePath;
        this.shareType = shareType;
        this.shareWith = shareWith;
        this.publicUpload = publicUpload;
        this.password = password;
        this.permissions = permissions;
        this.getShareDetails = getShareDetails;        // defaults to false for backwards compatibility
        this.note = note;
        this.attributes = attributes;
    }

    public CreateShareRemoteOperation(
            String remoteFilePath,
            ShareType shareType,
            String shareWith,
            boolean publicUpload,
            String password,
            int permissions) {
        this(remoteFilePath, shareType, shareWith, publicUpload, password, permissions, false, "", null);
    }

    public CreateShareRemoteOperation(
            String remoteFilePath,
            ShareType shareType,
            String shareWith,
            boolean publicUpload,
            String password,
            int permissions,
            String note,
            String attributes) {
        this(remoteFilePath, shareType, shareWith, publicUpload, password, permissions, false, note, attributes);
    }

    public CreateShareRemoteOperation(
            String remoteFilePath,
            ShareType shareType,
            String shareWith,
            boolean publicUpload,
            String password,
            int permissions,
            boolean getShareDetails) {
        this(remoteFilePath, shareType, shareWith, publicUpload, password, permissions, getShareDetails, "", null);
    }

    public boolean isGettingShareDetails() {
        return getShareDetails;
    }

    public void setGetShareDetails(boolean set) {
        getShareDetails = set;
    }

    @Override
    protected RemoteOperationResult<List<OCShare>> run(OwnCloudClient client) {
        RemoteOperationResult<List<OCShare>> result;
        int status;

        Utf8PostMethod post = null;

        try {
            // Post Method
            post = new Utf8PostMethod(client.getBaseUri() + ShareUtils.SHARING_API_PATH);

            post.setRequestHeader(CONTENT_TYPE, FORM_URLENCODED);

            post.addParameter(PARAM_PATH, remoteFilePath);
            post.addParameter(PARAM_SHARE_TYPE, Integer.toString(shareType.getValue()));
            post.addParameter(PARAM_SHARE_WITH, shareWith);
            if (publicUpload) {
                post.addParameter(PARAM_PUBLIC_UPLOAD, Boolean.toString(true));
            }
            if (password != null && password.length() > 0) {
                post.addParameter(PARAM_PASSWORD, password);
            }
            if (OCShare.NO_PERMISSION != permissions) {
                post.addParameter(PARAM_PERMISSIONS, Integer.toString(permissions));
            }

            if (!TextUtils.isEmpty(note)) {
                post.addParameter(PARAM_NOTE, note);
            }

            if (!TextUtils.isEmpty(attributes)) {
                post.addParameter(PARAM_ATTRIBUTES, attributes);
            }

            post.addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE);

            status = client.executeMethod(post);

            if (isSuccess(status)) {
                String response = post.getResponseBodyAsString();

                ShareToRemoteOperationResultParser parser = new ShareToRemoteOperationResultParser(
                        new ShareXMLParser()
                );
                parser.setOneOrMoreSharesRequired(true);
                parser.setServerBaseUri(client.getBaseUri());
                result = parser.parse(response);

                if (result.isSuccess() && getShareDetails) {
                    // retrieve more info - POST only returns the index of the new share
                    OCShare emptyShare = result.getResultData().get(0);
                    GetShareRemoteOperation getInfo = new GetShareRemoteOperation(emptyShare.getRemoteId());
                    result = getInfo.execute(client);
                }

            } else {
                result = new RemoteOperationResult<>(false, post);
            }

        } catch (IOException e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG, "Exception while Creating New Share", e);

        } finally {
            if (post != null) {
                post.releaseConnection();
            }
        }
        return result;
    }

    private boolean isSuccess(int status) {
        return status == HttpStatus.SC_OK || status == HttpStatus.SC_FORBIDDEN;
    }

}
