/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2019 Tobias Kaminsky
 *   Copyright (C) 2019 Nextcloud GmbH
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 *   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 *   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
 *
 */

package com.owncloud.android.lib.resources.files;

import android.util.Log;

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.network.WebdavEntry;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.model.RemoteFile;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.ReportMethod;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


/**
 * Get all favorited files
 */
public class GetFavoritesRemoteOperation extends RemoteOperation {

    private static final String TAG = GetFavoritesRemoteOperation.class.getSimpleName();
    private static final int RESTORE_READ_TIMEOUT = 30000;
    private static final int RESTORE_CONNECTION_TIMEOUT = 5000;

    private ArrayList<Object> favorites;

    /**
     * Constructor
     */
    public GetFavoritesRemoteOperation() {
    }

    /**
     * Performs the operation.
     *
     * @param client Client object to communicate with the remote Nextcloud server
     */
    @Override
    protected RemoteOperationResult run(OwnCloudClient client) {

        RemoteOperationResult result;
        try {
            String userId = client.getUserId();
            String url = client.getNewWebdavUri() + "/files/" + userId + "/";

            Document query;
            query = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element filterFilesElement = query.createElementNS(WebdavEntry.NAMESPACE_OC, "oc:filter-files");
            Element propElement = query.createElementNS("DAV:", "d:prop");

            Element remoteIdElement = query.createElementNS(WebdavEntry.NAMESPACE_OC, "oc:id");
            Element creationDate = query.createElementNS(WebdavEntry.NAMESPACE_DAV, "d:creationdate");
            Element contentLengthElement = query.createElementNS(WebdavEntry.NAMESPACE_DAV, "d:getcontentlength");
            Element contentTypeElement = query.createElementNS("DAV:", "d:getcontenttype");
            Element lastModifiedElement = query.createElementNS(WebdavEntry.NAMESPACE_DAV, "d:getlastmodified");
            Element etagElement = query.createElementNS(WebdavEntry.NAMESPACE_DAV, "d:getetag");
            Element permissionsElement = query.createElementNS(WebdavEntry.NAMESPACE_OC, "oc:permissions");
            Element sizeElement = query.createElementNS(WebdavEntry.NAMESPACE_OC, "oc:size");
            Element isFavoriteElement = query.createElementNS(WebdavEntry.NAMESPACE_OC, "oc:favorite");
            Element encryptedElement = query.createElementNS(WebdavEntry.NAMESPACE_NC, "nc:is-encrypted");
            Element mountTypeElement = query.createElementNS(WebdavEntry.NAMESPACE_NC, "nc:mount-type");
            Element ownerIdElement = query.createElementNS(WebdavEntry.NAMESPACE_OC, "oc:owner-id");
            Element displayNameElement = query.createElementNS(WebdavEntry.NAMESPACE_OC, "oc:owner-display-name");
            Element unreadCommentElement = query.createElementNS(WebdavEntry.NAMESPACE_OC, "oc:comments-unread");
            Element hasPreviewElement = query.createElementNS(WebdavEntry.NAMESPACE_NC, "nc:has-preview");
            Element noteElement = query.createElementNS(WebdavEntry.NAMESPACE_NC, "nc:note");

            propElement.appendChild(remoteIdElement);
            propElement.appendChild(creationDate);
            propElement.appendChild(contentLengthElement);
            propElement.appendChild(contentTypeElement);
            propElement.appendChild(lastModifiedElement);
            propElement.appendChild(etagElement);
            propElement.appendChild(permissionsElement);
            propElement.appendChild(sizeElement);
            propElement.appendChild(isFavoriteElement);
            propElement.appendChild(encryptedElement);
            propElement.appendChild(mountTypeElement);
            propElement.appendChild(ownerIdElement);
            propElement.appendChild(displayNameElement);
            propElement.appendChild(unreadCommentElement);
            propElement.appendChild(hasPreviewElement);
            propElement.appendChild(noteElement);

            Element filterRulesElement = query.createElementNS(WebdavEntry.NAMESPACE_OC, "oc:filter-rules");
            Element favoriteElement = query.createElementNS(WebdavEntry.NAMESPACE_OC, "oc:favorite");
            favoriteElement.appendChild(query.createTextNode("1"));

            filterRulesElement.appendChild(favoriteElement);

            filterFilesElement.appendChild(propElement);
            filterFilesElement.appendChild(filterRulesElement);

            ReportInfo reportInfo = new ReportInfo(filterFilesElement, DavConstants.DEPTH_INFINITY);

            ReportMethod report = new ReportMethod(url, reportInfo);
            int status = client.executeMethod(report, RESTORE_READ_TIMEOUT, RESTORE_CONNECTION_TIMEOUT);

            result = new RemoteOperationResult(isSuccess(status), report);

            if (isSuccess(status)) {
                MultiStatus dataInServer = report.getResponseBodyAsMultiStatus();
                readData(dataInServer, client, userId);
                result.setData(favorites);
            }

            client.exhaustResponse(report.getResponseBodyAsStream());
        } catch (IOException | ParserConfigurationException | DavException e) {
            result = new RemoteOperationResult(e);
            Log.e(TAG, "Get all favorites failed: " + result.getLogMessage(), e);
        }

        return result;
    }

    private void readData(MultiStatus remoteData, OwnCloudClient client, String userId) {
        favorites = new ArrayList<>();

        // loop to update every child
        RemoteFile remoteFile;
        WebdavEntry webdavEntry;
        for (MultiStatusResponse response : remoteData.getResponses()) {
            /// new OCFile instance with the data from the server
            webdavEntry = new WebdavEntry(response, client.getNewWebdavUri().getPath() + "/files/" + userId);
            remoteFile = fillOCFile(webdavEntry);
            favorites.add(remoteFile);
        }

    }

    // todo make it common
    private RemoteFile fillOCFile(WebdavEntry we) {
        RemoteFile file = new RemoteFile(we.decodedPath());
        file.setCreationTimestamp(we.getCreateTimestamp());
        file.setLength(we.getContentLength());
        file.setMimeType(we.getContentType());
        file.setModifiedTimestamp(we.getModifiedTimestamp());
        file.setEtag(we.getETag());
        file.setPermissions(we.getPermissions());
        file.setRemoteId(we.getRemoteId());
        file.setSize(we.getSize());
        file.setFavorite(we.isFavorite());
        file.setEncrypted(we.isEncrypted());
        file.setMountType(we.getMountType());
        file.setOwnerId(we.getOwnerId());
        file.setOwnerDisplayName(we.getOwnerDisplayName());
        file.setUnreadCommentsCount(we.getUnreadCommentsCount());
        file.setHasPreview(we.isHasPreview());
        file.setNote(we.getNote());

        return file;
    }

    private boolean isSuccess(int status) {
        return status == HttpStatus.SC_MULTI_STATUS || status == HttpStatus.SC_OK;
    }
}
