/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2018-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2018-2021 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2015 ownCloud Inc.
 * SPDX-FileCopyrightText: 2015 David A. Velasco <dvelasco@solidgear.es>
 * SPDX-FileCopyrightText: 2016 Juan Carlos González Cabrero <malkomich@gmail.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.shares;

import android.net.Uri;
import android.util.Pair;

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


/**
 * Updates parameters of an existing Share resource, known its remote ID.
 * <p>
 * Allow updating several parameters, triggering a request to the server per parameter.
 */
public class UpdateShareRemoteOperation extends RemoteOperation {

    private static final String TAG = GetShareRemoteOperation.class.getSimpleName();

    private static final String PARAM_PASSWORD = "password";
    private static final String PARAM_EXPIRATION_DATE = "expireDate";
    private static final String PARAM_PERMISSIONS = "permissions";
    private static final String PARAM_NOTE = "note";
    private static final String PARAM_HIDE_DOWNLOAD = "hideDownload";
    private static final String PARAM_LABEL = "label";
    private static final String FORMAT_EXPIRATION_DATE = "yyyy-MM-dd";
    private static final String ENTITY_CONTENT_TYPE = "application/x-www-form-urlencoded";
    private static final String ENTITY_CHARSET = "UTF-8";


    /**
     * Identifier of the share to update
     */
    private final long remoteId;

    /**
     * Password to set for the public link
     */
    private String password;

    /**
     * Expiration date to set for the public link
     */
    private long expirationDateInMillis;

    /**
     * Access permissions for the file bound to the share
     */
    private int permissions;

    /**
     * Permission if file can be downloaded via share link (only for single file)
     */
    private Boolean hideFileDownload;

    private String note;
    private String label;


    /**
     * Constructor. No update is initialized by default, need to be applied with setters below.
     *
     * @param remoteId Identifier of the share to update.
     */
    public UpdateShareRemoteOperation(long remoteId) {
        this.remoteId = remoteId;
        password = null;               // no update
        expirationDateInMillis = 0;    // no update
        note = null;
        label = null;
    }


    /**
     * Set password to update in Share resource.
     *
     * @param password Password to set to the target share.
     *                 Empty string clears the current password.
     *                 Null results in no update applied to the password.
     */
    public void setPassword(String password) {
        this.password = password;
    }


    /**
     * Set expiration date to update in Share resource.
     *
     * @param expirationDateInMillis Expiration date to set to the target share.
     *                               A negative value clears the current expiration date.
     *                               Zero value (start-of-epoch) results in no update done on
     *                               the expiration date.
     */
    public void setExpirationDate(long expirationDateInMillis) {
        this.expirationDateInMillis = expirationDateInMillis;
    }


    /**
     * Set permissions to update in Share resource.
     *
     * @param permissions Permissions to set to the target share.
     *                    Values <= 0 result in no update applied to the permissions.
     */
    public void setPermissions(int permissions) {
        this.permissions = permissions;
    }

    public void setHideFileDownload(Boolean hideFileDownload) {
        this.hideFileDownload = hideFileDownload;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    protected RemoteOperationResult<List<OCShare>> run(OwnCloudClient client) {
        RemoteOperationResult<List<OCShare>> result;

        /// prepare array of parameters to update
        List<Pair<String, String>> parametersToUpdate = new ArrayList<>();
        if (password != null) {
            parametersToUpdate.add(new Pair<>(PARAM_PASSWORD, password));
        }

        if (expirationDateInMillis < 0) {
            // clear expiration date
            parametersToUpdate.add(new Pair<>(PARAM_EXPIRATION_DATE, ""));
        } else if (expirationDateInMillis > 0) {
            // set expiration date
            DateFormat dateFormat = new SimpleDateFormat(FORMAT_EXPIRATION_DATE, Locale.US);
            Calendar expirationDate = Calendar.getInstance();
            expirationDate.setTimeInMillis(expirationDateInMillis);
            String formattedExpirationDate = dateFormat.format(expirationDate.getTime());
            parametersToUpdate.add(new Pair<>(PARAM_EXPIRATION_DATE, formattedExpirationDate));
        }
        
        if (permissions > 0) {
            parametersToUpdate.add(new Pair<>(PARAM_PERMISSIONS, Integer.toString(permissions)));
        }

        if (hideFileDownload != null) {
            parametersToUpdate.add(new Pair<>(PARAM_HIDE_DOWNLOAD, Boolean.toString(hideFileDownload)));
        }

        PutMethod put = null;
        try {
            if (note != null) {
                parametersToUpdate.add(new Pair<>(PARAM_NOTE, URLEncoder.encode(note, ENTITY_CHARSET)));
            }

            if (label != null) {
                parametersToUpdate.add(new Pair<>(PARAM_LABEL, URLEncoder.encode(label, ENTITY_CHARSET)));
            }

            Uri requestUri = client.getBaseUri();
            Uri.Builder uriBuilder = requestUri.buildUpon();
            uriBuilder.appendEncodedPath(ShareUtils.SHARING_API_PATH.substring(1));
            uriBuilder.appendEncodedPath(Long.toString(remoteId));
            String uriString = uriBuilder.build().toString();

            StringBuilder bodyBuilder = new StringBuilder();
            for (int i = 0; i < parametersToUpdate.size(); i++) {
                Pair<String, String> param = parametersToUpdate.get(i);
                bodyBuilder.append(param.first)
                    .append("=")
                    .append(param.second);

                if (i < parametersToUpdate.size() - 1) {
                    bodyBuilder.append("&");
                }
            }

            put = new PutMethod(uriString);
            put.addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE);
            put.setRequestEntity(new StringRequestEntity(
                bodyBuilder.toString(),
                ENTITY_CONTENT_TYPE,
                ENTITY_CHARSET
            ));

            int status = client.executeMethod(put);
            if (status == HttpStatus.SC_OK || status == HttpStatus.SC_BAD_REQUEST) {
                String response = put.getResponseBodyAsString();
                final var shareXMLParser = new ShareXMLParser();
                final var parser = new ShareToRemoteOperationResultParser(shareXMLParser);
                parser.setServerBaseUri(client.getBaseUri());
                result = parser.parse(response);
            } else {
                result = new RemoteOperationResult<>(false, put);
            }
        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG, "Exception while updating remote share ", e);
        } finally {
            if (put != null) {
                put.releaseConnection();
            }
        }

        return result;
    }
}
