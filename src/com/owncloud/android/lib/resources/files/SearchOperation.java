/*
 * Nextcloud Android client application
 *
 * @author Mario Danic
 * Copyright (C) 2017 Mario Danic
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.owncloud.android.lib.resources.files;

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.network.WebdavEntry;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.WebDavFileUtils;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.client.methods.OptionsMethod;
import org.apache.jackrabbit.webdav.search.SearchInfo;
import org.apache.jackrabbit.webdav.xml.Namespace;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Remote operation performing the search in the Nextcloud server.
 */
public class SearchOperation extends RemoteOperation {

    private static final String HEADER_CONTENT_TYPE_VALUE = "text/xml";

    private static final String DAV_NAMESPACE = "DAV:";

    public enum SearchType {
        FILE_SEARCH,
        FAVORITE_SEARCH,
        CONTENT_TYPE_SEARCH,
        RECENTLY_MODIFIED_SEARCH,
        RECENTLY_ADDED_SEARCH,
        SHARED_SEARCH,
        GALLERY_SEARCH,
        FULL_NEXT_SEARCH_FILE_SEARCH
    }

    private String searchQuery;
    private String userId;
    private SearchType searchType;
    private boolean filterOutFiles;

    public SearchOperation(String query, SearchType searchType, boolean filterOutFiles, String userId) {
        this.searchQuery = query;
        this.searchType = searchType;
        this.filterOutFiles = filterOutFiles;
        this.userId = userId;
    }

    @Override
    protected RemoteOperationResult run(OwnCloudClient client) {
        RemoteOperationResult result = null;
        SearchMethod searchMethod = null;
        OptionsMethod optionsMethod = null;

        String webDavUrl = client.getNewWebdavUri(false).toString();
        optionsMethod = new OptionsMethod(webDavUrl);

        try {
            int optionsStatus = client.executeMethod(optionsMethod);
            boolean isSearchSupported = optionsMethod.isAllowed("SEARCH");
            
            if (isSearchSupported) {
                searchMethod = new SearchMethod(webDavUrl, new SearchInfo("NC", Namespace.XMLNS_NAMESPACE, "NC"));

                int status = client.executeMethod(searchMethod);

                // check and process response
                boolean isSuccess = (status == HttpStatus.SC_MULTI_STATUS || status == HttpStatus.SC_OK);

                if (isSuccess) {
                    // get data from remote folder
                    MultiStatus dataInServer = searchMethod.getResponseBodyAsMultiStatus();
                    WebDavFileUtils webDavFileUtils = new WebDavFileUtils();
                    ArrayList<Object> mFolderAndFiles = webDavFileUtils.readData(dataInServer, client, false, true,
                            userId);

                    // Result of the operation
                    result = new RemoteOperationResult(true, status, searchMethod.getResponseHeaders());
                    // Add data to the result
                    if (result.isSuccess()) {
                        result.setData(mFolderAndFiles);
                    }
                } else {
                    // synchronization failed
                    client.exhaustResponse(searchMethod.getResponseBodyAsStream());
                    result = new RemoteOperationResult(false, status, searchMethod.getResponseHeaders());
                }
            } else {
                client.exhaustResponse(optionsMethod.getResponseBodyAsStream());
                result = new RemoteOperationResult(false, optionsStatus, optionsMethod.getResponseHeaders());
            }

        } catch (Exception e) {
            result = new RemoteOperationResult(e);
        } finally {
            if (searchMethod != null) {
                searchMethod.releaseConnection();  // let the connection available for other methods
            }

            if (optionsMethod != null) {
                optionsMethod.releaseConnection();
            }
        }
        return result;
    }

    private class SearchMethod extends org.apache.jackrabbit.webdav.client.methods.SearchMethod {

        public SearchMethod(String uri, String statement, String language) throws IOException {
            super(uri, statement, language);
        }

        public SearchMethod(String uri, String statement, String language, Namespace languageNamespace) throws IOException {
            super(uri, statement, language, languageNamespace);
        }

        public SearchMethod(String uri, SearchInfo searchInfo) throws IOException {
            super(uri, searchInfo);
            setRequestHeader(HEADER_CONTENT_TYPE, HEADER_CONTENT_TYPE_VALUE);
            setRequestBody(createQuery());
        }

    }

    private Document createQuery() {

        String internalSearchString = searchQuery;

        if (searchType == SearchType.FAVORITE_SEARCH) {
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
        Element permissionsElement = query.createElementNS(WebdavEntry.NAMESPACE_OC, "oc:permissions");
        Element remoteIdElement = query.createElementNS(WebdavEntry.NAMESPACE_OC, "oc:id");
        Element sizeElement = query.createElementNS(WebdavEntry.NAMESPACE_OC, "oc:size");
        Element favoriteElement = query.createElementNS(WebdavEntry.NAMESPACE_OC, "oc:favorite");

        selectPropsElement.appendChild(displayNameElement);
        selectPropsElement.appendChild(contentTypeElement);
        selectPropsElement.appendChild(resourceTypeElement);
        selectPropsElement.appendChild(contentLengthElement);
        selectPropsElement.appendChild(lastModifiedElement);
        selectPropsElement.appendChild(creationDate);
        selectPropsElement.appendChild(etagElement);
        selectPropsElement.appendChild(quotaUsedElement);
        selectPropsElement.appendChild(quotaAvailableElement);
        selectPropsElement.appendChild(permissionsElement);
        selectPropsElement.appendChild(remoteIdElement);
        selectPropsElement.appendChild(sizeElement);
        selectPropsElement.appendChild(favoriteElement);

        Element fromElement = query.createElementNS(DAV_NAMESPACE, "d:from");
        Element scopeElement = query.createElementNS(DAV_NAMESPACE, "d:scope");
        Element hrefElement = query.createElementNS(DAV_NAMESPACE, "d:href");
        Element depthElement = query.createElementNS(DAV_NAMESPACE, "d:depth");
        Text hrefTextElement = query.createTextNode("/files/" + userId);
        Text depthTextElement = query.createTextNode("infinity");
        Element whereElement = query.createElementNS(DAV_NAMESPACE, "d:where");
        Element folderElement = null;
        if (filterOutFiles) {
            folderElement = query.createElementNS(DAV_NAMESPACE, "d:is-collection");
        }
        Element equalsElement;
        if (searchType == SearchType.FAVORITE_SEARCH) {
            equalsElement = query.createElementNS(DAV_NAMESPACE, "d:eq");
        } else if (searchType == SearchType.RECENTLY_MODIFIED_SEARCH ||
                searchType == SearchType.RECENTLY_ADDED_SEARCH) {
            equalsElement = query.createElementNS(DAV_NAMESPACE, "d:gt");
        } else if (searchType == SearchType.GALLERY_SEARCH) {
            equalsElement = query.createElementNS(DAV_NAMESPACE, "d:or");
        } else {
            equalsElement = query.createElementNS(DAV_NAMESPACE, "d:like");
        }

        Element propElement = null;
        Element queryElement = null;
        Element literalElement = null;
        Text literalTextElement = null;
        Element imageLikeElement = null;
        Element videoLikeElement = null;

        if (searchType != SearchType.GALLERY_SEARCH) {
            propElement = query.createElementNS(DAV_NAMESPACE, "d:prop");
            queryElement = null;
            if (searchType == SearchType.CONTENT_TYPE_SEARCH) {
                queryElement = query.createElementNS(DAV_NAMESPACE, "d:getcontenttype");
            } else if (searchType == SearchType.FILE_SEARCH) {
                queryElement = query.createElementNS(DAV_NAMESPACE, "d:displayname");
            } else if (searchType == SearchType.FAVORITE_SEARCH) {
                queryElement = query.createElementNS(WebdavEntry.NAMESPACE_OC, "oc:favorite");
            } else if (searchType == SearchType.RECENTLY_MODIFIED_SEARCH) {
                queryElement = query.createElementNS(DAV_NAMESPACE, "d:getlastmodified");
            } else if (searchType == SearchType.RECENTLY_ADDED_SEARCH) {
                queryElement = query.createElementNS(DAV_NAMESPACE, "d:creationdate");
            }
            literalElement = query.createElementNS(DAV_NAMESPACE, "d:literal");
            if (searchType != SearchType.RECENTLY_MODIFIED_SEARCH && searchType != SearchType.RECENTLY_ADDED_SEARCH) {
                if (searchType == SearchType.FILE_SEARCH) {
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

        // Disabling order for now, but will leave the code in place for the future

        /*if (searchType == SearchType.RECENTLY_MODIFIED_SEARCH || searchType == SearchType.FAVORITE_SEARCH) {
            Element orderElement = query.createElementNS(DAV_NAMESPACE, "d:order");
            orderByElement.appendChild(orderElement);
            Element orderPropElement = query.createElementNS(DAV_NAMESPACE, "d:prop");
            orderElement.appendChild(orderPropElement);
            Element orderPropElementValue = query.createElementNS(DAV_NAMESPACE, "d:getlastmodified");
            orderPropElement.appendChild(orderPropElementValue);
            Element orderAscDescElement = query.createElementNS(DAV_NAMESPACE, "d:descending");
            orderElement.appendChild(orderAscDescElement);
        }*/

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
        if (folderElement != null) {
            Element andElement = query.createElementNS(DAV_NAMESPACE, "d:and");
            andElement.appendChild(folderElement);
            andElement.appendChild(equalsElement);
            whereElement.appendChild(andElement);
        } else {
            whereElement.appendChild(equalsElement);
        }

        if (searchType != SearchType.GALLERY_SEARCH) {
            equalsElement.appendChild(propElement);
            equalsElement.appendChild(literalElement);
            propElement.appendChild(queryElement);
            literalElement.appendChild(literalTextElement);
        } else {
            equalsElement.appendChild(imageLikeElement);
            equalsElement.appendChild(videoLikeElement);
        }
        basicSearchElement.appendChild(orderByElement);

        return query;
    }

}

