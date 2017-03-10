/**
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
        RECENTLY_ADDED_SEARCH
    }

    private String searchQuery;
    private SearchType searchType;

    public SearchOperation(String query, SearchType searchType) {
        this.searchQuery = query;
        this.searchType = searchType;
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

                searchMethod = new SearchMethod(webDavUrl, new SearchInfo("NC",
                        Namespace.XMLNS_NAMESPACE, "NC"));

                int status = client.executeMethod(searchMethod);

                // check and process response
                boolean isSuccess = (
                        status == HttpStatus.SC_MULTI_STATUS ||
                                status == HttpStatus.SC_OK
                );
                if (isSuccess) {
                    // get data from remote folder
                    MultiStatus dataInServer = searchMethod.getResponseBodyAsMultiStatus();
                    WebDavFileUtils webDavFileUtils = new WebDavFileUtils();
                    ArrayList<Object> mFolderAndFiles = webDavFileUtils.readData(dataInServer, client, false, true,
                            client.getCredentials().getUsername());

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
        Text hrefTextElement = query.createTextNode("/files/" + getClient().getCredentials().getUsername());
        Text depthTextElement = query.createTextNode("infinity");
        Element whereElement = query.createElementNS(DAV_NAMESPACE, "d:where");
        Element equalsElement;
        if (searchType == SearchType.FAVORITE_SEARCH) {
            equalsElement = query.createElementNS(DAV_NAMESPACE, "d:eq");
        } else if (searchType == SearchType.RECENTLY_MODIFIED_SEARCH ||
                searchType == SearchType.RECENTLY_ADDED_SEARCH){
            equalsElement = query.createElementNS(DAV_NAMESPACE, "d:gt");
        } else {
            equalsElement = query.createElementNS(DAV_NAMESPACE, "d:like");
        }
        Element propElement = query.createElementNS(DAV_NAMESPACE, "d:prop");
        Element queryElement = null;
        if (searchType == SearchType.CONTENT_TYPE_SEARCH) {
            queryElement = query.createElementNS(DAV_NAMESPACE, "d:getcontenttype");
        } else if (searchType == SearchType.FILE_SEARCH){
            queryElement = query.createElementNS(DAV_NAMESPACE, "d:displayname");
        } else if (searchType == SearchType.FAVORITE_SEARCH) {
            queryElement = query.createElementNS(WebdavEntry.NAMESPACE_OC, "oc:favorite");
        } else if (searchType == SearchType.RECENTLY_MODIFIED_SEARCH) {
            queryElement = query.createElementNS(DAV_NAMESPACE, "d:getlastmodifed");
        } else if (searchType == SearchType.RECENTLY_ADDED_SEARCH) {
            queryElement = query.createElementNS(DAV_NAMESPACE, "d:creationdate");
        }
        Element literalElement = query.createElementNS(DAV_NAMESPACE, "d:literal");
        Text literalTextElement;
        if (searchType != SearchType.RECENTLY_MODIFIED_SEARCH && searchType != SearchType.RECENTLY_ADDED_SEARCH) {
            literalTextElement = query.createTextNode(internalSearchString);
        } else {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
            dateFormat.setTimeZone(TimeZone.getDefault());
            Date date = new Date();
            String formattedDateString = dateFormat.format(date);
            literalTextElement = query.createTextNode(formattedDateString);
        }

        Element orderByElement = query.createElementNS(DAV_NAMESPACE, "d:orderby");

        if (searchType == SearchType.RECENTLY_MODIFIED_SEARCH) {
            Element orderElement = query.createElementNS(DAV_NAMESPACE, "d:order");
            Element orderPropElement = query.createElementNS(DAV_NAMESPACE, "d:prop");
            Element orderPropElementValue = query.createElementNS(DAV_NAMESPACE, "d:getlastmodified");
            Element orderAscDescElement = query.createElementNS(DAV_NAMESPACE, "d:descending");

            orderPropElement.appendChild(orderPropElementValue);
            orderElement.appendChild(orderPropElement);
            orderElement.appendChild(orderAscDescElement);
            orderByElement.appendChild(orderElement);
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
        whereElement.appendChild(equalsElement);
        equalsElement.appendChild(propElement);
        equalsElement.appendChild(literalElement);
        propElement.appendChild(queryElement);
        literalElement.appendChild(literalTextElement);
        basicSearchElement.appendChild(orderByElement);

        return query;
    }


}
