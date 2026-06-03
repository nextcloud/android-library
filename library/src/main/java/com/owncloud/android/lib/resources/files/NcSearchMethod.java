/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2019 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files;

import com.owncloud.android.lib.resources.status.NextcloudVersion;
import com.owncloud.android.lib.resources.status.OCCapability;

import org.apache.jackrabbit.webdav.search.SearchInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static com.owncloud.android.lib.common.network.WebdavEntry.EXTENDED_PROPERTY_IS_ENCRYPTED;
import static com.owncloud.android.lib.common.network.WebdavEntry.EXTENDED_PROPERTY_NAME_LOCAL_ID;
import static com.owncloud.android.lib.common.network.WebdavEntry.EXTENDED_PROPERTY_MOUNT_TYPE;
import static com.owncloud.android.lib.common.network.WebdavEntry.EXTENDED_PROPERTY_OWNER_ID;
import static com.owncloud.android.lib.common.network.WebdavEntry.EXTENDED_PROPERTY_OWNER_DISPLAY_NAME;
import static com.owncloud.android.lib.common.network.WebdavEntry.EXTENDED_PROPERTY_UNREAD_COMMENTS;
import static com.owncloud.android.lib.common.network.WebdavEntry.EXTENDED_PROPERTY_NOTE;
import static com.owncloud.android.lib.common.network.WebdavEntry.EXTENDED_PROPERTY_SHAREES;
import static com.owncloud.android.lib.common.network.WebdavEntry.EXTENDED_PROPERTY_SHARE_TYPES;
import static com.owncloud.android.lib.common.network.WebdavEntry.EXTENDED_PROPERTY_SHARE_ATTRIBUTES;
import static com.owncloud.android.lib.common.network.WebdavEntry.EXTENDED_PROPERTY_HIDDEN;
import static com.owncloud.android.lib.common.network.WebdavEntry.EXTENDED_PROPERTY_RICH_WORKSPACE;
import static com.owncloud.android.lib.common.network.WebdavEntry.EXTENDED_PROPERTY_SYSTEM_TAGS;
import static com.owncloud.android.lib.common.network.WebdavEntry.EXTENDED_PROPERTY_CREATION_TIME;
import static com.owncloud.android.lib.common.network.WebdavEntry.EXTENDED_PROPERTY_UPLOAD_TIME;
import static com.owncloud.android.lib.common.network.WebdavEntry.EXTENDED_PROPERTY_METADATA_LIVE_PHOTO;
import static com.owncloud.android.lib.common.network.WebdavEntry.EXTENDED_PROPERTY_FILE_DOWNLOAD_LIMITS;
import static com.owncloud.android.lib.common.network.WebdavEntry.EXTENDED_PROPERTY_METADATA_PHOTOS_SIZE;
import static com.owncloud.android.lib.common.network.WebdavEntry.EXTENDED_PROPERTY_METADATA_PHOTOS_GPS;
import static com.owncloud.android.lib.common.network.WebdavEntry.EXTENDED_PROPERTY_METADATA_SIZE;
import static com.owncloud.android.lib.common.network.WebdavEntry.EXTENDED_PROPERTY_METADATA_GPS;
import static com.owncloud.android.lib.common.network.WebdavEntry.EXTENDED_PROPERTY_LOCK;
import static com.owncloud.android.lib.common.network.WebdavEntry.EXTENDED_PROPERTY_LOCK_OWNER_TYPE;
import static com.owncloud.android.lib.common.network.WebdavEntry.EXTENDED_PROPERTY_LOCK_OWNER;
import static com.owncloud.android.lib.common.network.WebdavEntry.EXTENDED_PROPERTY_LOCK_OWNER_DISPLAY_NAME;
import static com.owncloud.android.lib.common.network.WebdavEntry.EXTENDED_PROPERTY_LOCK_OWNER_EDITOR;
import static com.owncloud.android.lib.common.network.WebdavEntry.EXTENDED_PROPERTY_LOCK_TIME;
import static com.owncloud.android.lib.common.network.WebdavEntry.EXTENDED_PROPERTY_LOCK_TIMEOUT;
import static com.owncloud.android.lib.common.network.WebdavEntry.EXTENDED_PROPERTY_LOCK_TOKEN;
import static com.owncloud.android.lib.common.network.WebdavEntry.NAMESPACE_NC;
import static com.owncloud.android.lib.common.network.WebdavEntry.NAMESPACE_OC;

public class NcSearchMethod extends org.apache.jackrabbit.webdav.client.methods.SearchMethod {
    private static final String HEADER_CONTENT_TYPE_VALUE = "text/xml";
    private static final String DAV_NAMESPACE = "DAV:";

    private final SearchRemoteOperation.SearchType searchType;
    private final long timestamp;
    private final int limit;
    private final boolean filterOutFiles;
    private final OCCapability capability;
    private final String userId;
    private final Long startDate;
    private final Long endDate;

    @SuppressWarnings("PMD.ExcessiveParameterList")
    public NcSearchMethod(String uri, 
                          SearchInfo searchInfo,
                          SearchRemoteOperation.SearchType searchType,
                          String userId,
                          long timestamp,
                          int limit,
                          boolean filterOutFiles,
                          final OCCapability capability,
                          Long startDate,
                          Long endDate) throws IOException {
        super(uri, searchInfo);
        this.searchType = searchType;
        this.userId = userId;
        this.limit = limit;
        this.filterOutFiles = filterOutFiles;
        this.timestamp = timestamp;
        this.capability = capability;
        this.startDate = startDate;
        this.endDate = endDate;

        setRequestHeader(HEADER_CONTENT_TYPE, HEADER_CONTENT_TYPE_VALUE);
        setRequestBody(createQuery(searchInfo.getQuery()));
    }

    private Document createQuery(String searchQuery) {
        String internalSearchString = searchQuery;

        if (searchType == SearchRemoteOperation.SearchType.FAVORITE_SEARCH) {
            internalSearchString = "yes";
        }

        Document query;
        try {
            query = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException parserError) {
            System.err.println("ParserConfigurationException: " + parserError.getLocalizedMessage());
            return null;
        }

        // Create Nodes & Elements
        Element searchRequestElement = query.createElementNS(DAV_NAMESPACE, "d:searchrequest");
        Element basicSearchElement = query.createElementNS(DAV_NAMESPACE, "d:basicsearch");
        Element selectElement = query.createElementNS(DAV_NAMESPACE, "d:select");
        Element selectPropsElement = query.createElementNS(DAV_NAMESPACE, "d:prop");
        // get all
        Element displayNameElement = query.createElementNS(DAV_NAMESPACE, "d:displayname");
        Element contentTypeElement = query.createElementNS(DAV_NAMESPACE, "d:getcontenttype");
        Element resourceTypeElement = query.createElementNS(DAV_NAMESPACE, "d:resourcetype");
        Element contentLengthElement = query.createElementNS(DAV_NAMESPACE, "d:getcontentlength");
        Element lastModifiedElement = query.createElementNS(DAV_NAMESPACE, "d:getlastmodified");
        Element creationDate = query.createElementNS(DAV_NAMESPACE, "d:creationdate");
        Element etagElement = query.createElementNS(DAV_NAMESPACE, "d:getetag");
        Element quotaUsedElement = query.createElementNS(DAV_NAMESPACE, "d:quota-used-bytes");
        Element quotaAvailableElement = query.createElementNS(DAV_NAMESPACE, "d:quota-available-bytes");
        Element permissionsElement = query.createElementNS(NAMESPACE_OC, "oc:permissions");
        Element remoteIdElement = query.createElementNS(NAMESPACE_OC, "oc:id");
        Element localIdElement = query.createElementNS(NAMESPACE_OC, "oc:" + EXTENDED_PROPERTY_NAME_LOCAL_ID);
        Element sizeElement = query.createElementNS(NAMESPACE_OC, "oc:size");
        Element favoriteElement = query.createElementNS(NAMESPACE_OC, "oc:favorite");
        Element previewElement = query.createElementNS(NAMESPACE_OC, "nc:has-preview");
        Element encryptedElement = query.createElementNS(NAMESPACE_NC, EXTENDED_PROPERTY_IS_ENCRYPTED);

        // Additional properties parsed by WebdavEntry
        Element ownerIdElement = query.createElementNS(NAMESPACE_OC, "oc:" + EXTENDED_PROPERTY_OWNER_ID);
        Element ownerDisplayNameElement = query.createElementNS(NAMESPACE_OC, "oc:" + EXTENDED_PROPERTY_OWNER_DISPLAY_NAME);
        Element unreadCommentsElement = query.createElementNS(NAMESPACE_OC, "oc:" + EXTENDED_PROPERTY_UNREAD_COMMENTS);
        Element shareTypesElement = query.createElementNS(NAMESPACE_OC, "oc:" + EXTENDED_PROPERTY_SHARE_TYPES);
        Element mountTypeElement = query.createElementNS(NAMESPACE_NC, "nc:" + EXTENDED_PROPERTY_MOUNT_TYPE);
        Element noteElement = query.createElementNS(NAMESPACE_NC, "nc:" + EXTENDED_PROPERTY_NOTE);
        Element shareesElement = query.createElementNS(NAMESPACE_NC, "nc:" + EXTENDED_PROPERTY_SHAREES);
        Element shareAttributesElement = query.createElementNS(NAMESPACE_NC, "nc:" + EXTENDED_PROPERTY_SHARE_ATTRIBUTES);
        Element hiddenElement = query.createElementNS(NAMESPACE_NC, "nc:" + EXTENDED_PROPERTY_HIDDEN);
        Element richWorkspaceElement = query.createElementNS(NAMESPACE_NC, "nc:" + EXTENDED_PROPERTY_RICH_WORKSPACE);
        Element systemTagsElement = query.createElementNS(NAMESPACE_NC, "nc:" + EXTENDED_PROPERTY_SYSTEM_TAGS);
        Element creationTimeElement = query.createElementNS(NAMESPACE_NC, "nc:" + EXTENDED_PROPERTY_CREATION_TIME);
        Element uploadTimeElement = query.createElementNS(NAMESPACE_NC, "nc:" + EXTENDED_PROPERTY_UPLOAD_TIME);
        Element livePhotoElement = query.createElementNS(NAMESPACE_NC, "nc:" + EXTENDED_PROPERTY_METADATA_LIVE_PHOTO);
        Element downloadLimitsElement = query.createElementNS(NAMESPACE_NC, "nc:" + EXTENDED_PROPERTY_FILE_DOWNLOAD_LIMITS);
        Element metaPhotosSizeElement = query.createElementNS(NAMESPACE_NC, "nc:" + EXTENDED_PROPERTY_METADATA_PHOTOS_SIZE);
        Element metaPhotosGpsElement = query.createElementNS(NAMESPACE_NC, "nc:" + EXTENDED_PROPERTY_METADATA_PHOTOS_GPS);
        Element metaSizeElement = query.createElementNS(NAMESPACE_NC, "nc:" + EXTENDED_PROPERTY_METADATA_SIZE);
        Element metaGpsElement = query.createElementNS(NAMESPACE_NC, "nc:" + EXTENDED_PROPERTY_METADATA_GPS);
        Element lockElement = query.createElementNS(NAMESPACE_NC, "nc:" + EXTENDED_PROPERTY_LOCK);
        Element lockOwnerTypeElement = query.createElementNS(NAMESPACE_NC, "nc:" + EXTENDED_PROPERTY_LOCK_OWNER_TYPE);
        Element lockOwnerElement = query.createElementNS(NAMESPACE_NC, "nc:" + EXTENDED_PROPERTY_LOCK_OWNER);
        Element lockOwnerDisplayNameElement = query.createElementNS(NAMESPACE_NC, "nc:" + EXTENDED_PROPERTY_LOCK_OWNER_DISPLAY_NAME);
        Element lockOwnerEditorElement = query.createElementNS(NAMESPACE_NC, "nc:" + EXTENDED_PROPERTY_LOCK_OWNER_EDITOR);
        Element lockTimeElement = query.createElementNS(NAMESPACE_NC, "nc:" + EXTENDED_PROPERTY_LOCK_TIME);
        Element lockTimeoutElement = query.createElementNS(NAMESPACE_NC, "nc:" + EXTENDED_PROPERTY_LOCK_TIMEOUT);
        Element lockTokenElement = query.createElementNS(NAMESPACE_NC, "nc:" + EXTENDED_PROPERTY_LOCK_TOKEN);

        if (searchType != SearchRemoteOperation.SearchType.GALLERY_SEARCH) {
            selectPropsElement.appendChild(displayNameElement);
            selectPropsElement.appendChild(creationDate);
            selectPropsElement.appendChild(quotaUsedElement);
            selectPropsElement.appendChild(quotaAvailableElement);
            selectPropsElement.appendChild(sizeElement);
            selectPropsElement.appendChild(encryptedElement);
        }
        if (searchType == SearchRemoteOperation.SearchType.GALLERY_SEARCH) {
            selectPropsElement.appendChild(previewElement);
        }
        selectPropsElement.appendChild(contentTypeElement);
        selectPropsElement.appendChild(resourceTypeElement);
        selectPropsElement.appendChild(contentLengthElement);
        selectPropsElement.appendChild(lastModifiedElement);
        selectPropsElement.appendChild(etagElement);
        selectPropsElement.appendChild(remoteIdElement);
        selectPropsElement.appendChild(localIdElement);
        selectPropsElement.appendChild(favoriteElement);
        selectPropsElement.appendChild(permissionsElement);

        // Common additional properties for all search types
        selectPropsElement.appendChild(ownerIdElement);
        selectPropsElement.appendChild(ownerDisplayNameElement);
        selectPropsElement.appendChild(unreadCommentsElement);
        selectPropsElement.appendChild(shareTypesElement);
        selectPropsElement.appendChild(mountTypeElement);
        selectPropsElement.appendChild(noteElement);
        selectPropsElement.appendChild(shareesElement);
        selectPropsElement.appendChild(shareAttributesElement);
        selectPropsElement.appendChild(hiddenElement);
        selectPropsElement.appendChild(richWorkspaceElement);
        selectPropsElement.appendChild(systemTagsElement);
        selectPropsElement.appendChild(creationTimeElement);
        selectPropsElement.appendChild(uploadTimeElement);
        selectPropsElement.appendChild(livePhotoElement);
        selectPropsElement.appendChild(downloadLimitsElement);
        selectPropsElement.appendChild(metaPhotosSizeElement);
        selectPropsElement.appendChild(metaPhotosGpsElement);
        selectPropsElement.appendChild(metaSizeElement);
        selectPropsElement.appendChild(metaGpsElement);
        selectPropsElement.appendChild(lockElement);
        selectPropsElement.appendChild(lockOwnerTypeElement);
        selectPropsElement.appendChild(lockOwnerElement);
        selectPropsElement.appendChild(lockOwnerDisplayNameElement);
        selectPropsElement.appendChild(lockOwnerEditorElement);
        selectPropsElement.appendChild(lockTimeElement);
        selectPropsElement.appendChild(lockTimeoutElement);
        selectPropsElement.appendChild(lockTokenElement);
        if (searchType != SearchRemoteOperation.SearchType.GALLERY_SEARCH) {
            selectPropsElement.appendChild(previewElement);
        }

        Element fromElement = query.createElementNS(DAV_NAMESPACE, "d:from");
        Element scopeElement = query.createElementNS(DAV_NAMESPACE, "d:scope");
        Element hrefElement = query.createElementNS(DAV_NAMESPACE, "d:href");
        Element depthElement = query.createElementNS(DAV_NAMESPACE, "d:depth");
        Text hrefTextElement = query.createTextNode("/files/" + userId);
        Text depthTextElement = query.createTextNode("infinity");
        Element whereElement = query.createElementNS(DAV_NAMESPACE, "d:where");
        Element folderElement;
        Element equalsElement;

        switch (searchType) {
            case FAVORITE_SEARCH:
            case FILE_ID_SEARCH:
                equalsElement = query.createElementNS(DAV_NAMESPACE, "d:eq");
                break;

            case RECENTLY_MODIFIED_SEARCH:
                equalsElement = query.createElementNS(DAV_NAMESPACE, "d:gt");
                break;

            case GALLERY_SEARCH:
                equalsElement = query.createElementNS(DAV_NAMESPACE, "d:or");
                break;

            default:
                equalsElement = query.createElementNS(DAV_NAMESPACE, "d:like");
                break;
        }

        Element propElement = null;
        Element queryElement = null;
        Element literalElement = null;
        Text literalTextElement = null;
        Element imageLikeElement = null;
        Element videoLikeElement = null;

        if (searchType != SearchRemoteOperation.SearchType.GALLERY_SEARCH) {
            propElement = query.createElementNS(DAV_NAMESPACE, "d:prop");

            switch (searchType) {
                case PHOTO_SEARCH:
                    queryElement = query.createElementNS(DAV_NAMESPACE, "d:getcontenttype");
                    break;

                case FILE_SEARCH:
                    queryElement = query.createElementNS(DAV_NAMESPACE, "d:displayname");
                    break;

                case FAVORITE_SEARCH:
                    queryElement = query.createElementNS(NAMESPACE_OC, "oc:favorite");
                    break;

                case RECENTLY_MODIFIED_SEARCH:
                    queryElement = query.createElementNS(DAV_NAMESPACE, "d:getlastmodified");
                    break;

                case FILE_ID_SEARCH:
                    queryElement = query.createElementNS(NAMESPACE_OC, "oc:fileid");
                    break;

                default:
                    // no default
                    break;
            }

            literalElement = query.createElementNS(DAV_NAMESPACE, "d:literal");
            if (searchType != SearchRemoteOperation.SearchType.RECENTLY_MODIFIED_SEARCH) {
                if (searchType == SearchRemoteOperation.SearchType.FILE_SEARCH) {
                    internalSearchString = "%" + internalSearchString + "%";
                }
                literalTextElement = query.createTextNode(internalSearchString);
            } else {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                dateFormat.setTimeZone(TimeZone.getDefault());
                Date date = new Date();

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.add(Calendar.DAY_OF_YEAR, -7);
                date = calendar.getTime();

                String formattedDateString = dateFormat.format(date);
                literalTextElement = query.createTextNode(formattedDateString);
            }
        } else {
            imageLikeElement = query.createElementNS(DAV_NAMESPACE, "d:like");
            Element imagePropElement = query.createElementNS(DAV_NAMESPACE, "d:prop");
            Element imageQueryElement = query.createElementNS(DAV_NAMESPACE, "d:getcontenttype");
            Element imageLiteralElement = query.createElementNS(DAV_NAMESPACE, "d:literal");
            Text imageLiteralTextElement = query.createTextNode("image/%");
            videoLikeElement = query.createElementNS(DAV_NAMESPACE, "d:like");
            Element videoPropElement = query.createElementNS(DAV_NAMESPACE, "d:prop");
            Element videoQueryElement = query.createElementNS(DAV_NAMESPACE, "d:getcontenttype");
            Element videoLiteralElement = query.createElementNS(DAV_NAMESPACE, "d:literal");
            Text videoLiteralTextElement = query.createTextNode("video/%");

            videoLiteralElement.appendChild(videoLiteralTextElement);
            imageLiteralElement.appendChild(imageLiteralTextElement);

            videoPropElement.appendChild(videoQueryElement);
            videoLikeElement.appendChild(videoPropElement);
            videoLikeElement.appendChild(videoLiteralElement);


            imagePropElement.appendChild(imageQueryElement);
            imageLikeElement.appendChild(imagePropElement);
            imageLikeElement.appendChild(imageLiteralElement);

        }

        Element orderByElement = query.createElementNS(DAV_NAMESPACE, "d:orderby");

        if (searchType == SearchRemoteOperation.SearchType.PHOTO_SEARCH ||
                searchType == SearchRemoteOperation.SearchType.RECENTLY_MODIFIED_SEARCH ||
                searchType == SearchRemoteOperation.SearchType.GALLERY_SEARCH) {
            Element orderElement = query.createElementNS(DAV_NAMESPACE, "d:order");
            orderByElement.appendChild(orderElement);
            Element orderPropElement = query.createElementNS(DAV_NAMESPACE, "d:prop");
            orderElement.appendChild(orderPropElement);
            Element orderPropElementValue = query.createElementNS(DAV_NAMESPACE, "d:getlastmodified");
            orderPropElement.appendChild(orderPropElementValue);
            Element orderAscDescElement = query.createElementNS(DAV_NAMESPACE, "d:descending");
            orderElement.appendChild(orderAscDescElement);
        }

        // Build XML tree
        searchRequestElement.setAttribute("xmlns:oc", "http://nextcloud.com/ns");
        query.appendChild(searchRequestElement);
        searchRequestElement.appendChild(basicSearchElement);
        basicSearchElement.appendChild(selectElement);
        basicSearchElement.appendChild(fromElement);
        basicSearchElement.appendChild(whereElement);
        selectElement.appendChild(selectPropsElement);
        fromElement.appendChild(scopeElement);
        scopeElement.appendChild(hrefElement);
        scopeElement.appendChild(depthElement);
        hrefElement.appendChild(hrefTextElement);
        depthElement.appendChild(depthTextElement);

        if (filterOutFiles) {
            Element andElement = query.createElementNS(DAV_NAMESPACE, "d:and");
            folderElement = query.createElementNS(DAV_NAMESPACE, "d:is-collection");
            andElement.appendChild(folderElement);
            andElement.appendChild(equalsElement);
            whereElement.appendChild(andElement);
        } else if (timestamp != -1) {
            Element and = query.createElementNS(DAV_NAMESPACE, "d:and");
            Element lessThan = query.createElementNS(DAV_NAMESPACE, "d:lt");
            Element lastModified = query.createElementNS(DAV_NAMESPACE, "d:getlastmodified");
            Element literal = query.createElementNS(DAV_NAMESPACE, "d:literal");
            Element prop = query.createElementNS(DAV_NAMESPACE, "d:prop");
            prop.appendChild(lastModified);
            literal.setTextContent(String.valueOf(timestamp));

            lessThan.appendChild(prop);
            lessThan.appendChild(literal);

            and.appendChild(lessThan);
            and.appendChild(equalsElement);
            whereElement.appendChild(and);
        } else if (startDate != null && endDate != null) {
            Element and = query.createElementNS(DAV_NAMESPACE, "d:and");
            
            Element lessThanProp = query.createElementNS(DAV_NAMESPACE, "d:prop");
            Element lessThan = query.createElementNS(DAV_NAMESPACE, "d:lt");
            Element lessThanLiteral = query.createElementNS(DAV_NAMESPACE, "d:literal");
            Element lessThanLastModified = query.createElementNS(DAV_NAMESPACE, "d:getlastmodified");

            lessThanProp.appendChild(lessThanLastModified);
            lessThanLiteral.setTextContent(String.valueOf(endDate));
            
            lessThan.appendChild(lessThanProp);
            lessThan.appendChild(lessThanLiteral);

            Element greaterThanProp = query.createElementNS(DAV_NAMESPACE, "d:prop");
            Element greaterThan = query.createElementNS(DAV_NAMESPACE, "d:gt");
            Element greaterThanLiteral = query.createElementNS(DAV_NAMESPACE, "d:literal");
            Element greaterThanLastModified = query.createElementNS(DAV_NAMESPACE, "d:getlastmodified");

            greaterThanProp.appendChild(greaterThanLastModified);
            greaterThanLiteral.setTextContent(String.valueOf(startDate));
            
            greaterThan.appendChild(greaterThanProp);
            greaterThan.appendChild(greaterThanLiteral);

            and.appendChild(lessThan);
            and.appendChild(greaterThan);
            and.appendChild(equalsElement);
            whereElement.appendChild(and);
        } else {
            if (searchType == SearchRemoteOperation.SearchType.GALLERY_SEARCH
                    && capability.getVersion().isOlderThan(NextcloudVersion.nextcloud_22)) {
                Element and = query.createElementNS(DAV_NAMESPACE, "d:and");
                Element lessThan = query.createElementNS(DAV_NAMESPACE, "d:eq");
                Element lastModified = query.createElementNS(NAMESPACE_OC, "oc:owner-id");
                Element literal = query.createElementNS(DAV_NAMESPACE, "d:literal");
                Element prop = query.createElementNS(DAV_NAMESPACE, "d:prop");
                prop.appendChild(lastModified);
                literal.setTextContent(String.valueOf(userId));

                lessThan.appendChild(prop);
                lessThan.appendChild(literal);

                and.appendChild(lessThan);
                and.appendChild(equalsElement);
                whereElement.appendChild(and);
            } else {
                whereElement.appendChild(equalsElement);
            }
        }

        if (searchType == SearchRemoteOperation.SearchType.GALLERY_SEARCH) {
            equalsElement.appendChild(imageLikeElement);
            equalsElement.appendChild(videoLikeElement);
        } else {
            equalsElement.appendChild(propElement);
            equalsElement.appendChild(literalElement);
            if (queryElement != null) {
                propElement.appendChild(queryElement);
            }
            literalElement.appendChild(literalTextElement);
        }
        basicSearchElement.appendChild(orderByElement);

        if (limit > 0) {
            Element limitElement = query.createElementNS(DAV_NAMESPACE, "d:limit");
            Element nResultElement = query.createElementNS(DAV_NAMESPACE, "d:nresults");
            nResultElement.appendChild(query.createTextNode(String.valueOf(limit)));
            limitElement.appendChild(nResultElement);
            basicSearchElement.appendChild(limitElement);
        }

        return query;
    }
}
