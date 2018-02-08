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

import com.owncloud.android.lib.common.network.WebdavEntry;

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

public class NcSearchMethod extends org.apache.jackrabbit.webdav.client.methods.SearchMethod {
    private static final String HEADER_CONTENT_TYPE_VALUE = "text/xml";
    private static final String DAV_NAMESPACE = "DAV:";

    private SearchRemoteOperation.SearchType searchType;
    private long timestamp;
    private int limit;
    private boolean filterOutFiles;
    private String userId;

    public NcSearchMethod(String uri, SearchInfo searchInfo,
                          SearchRemoteOperation.SearchType searchType, String userId, long timestamp,
                          int limit, boolean filterOutFiles) throws IOException {
        super(uri, searchInfo);
        this.searchType = searchType;
        this.userId = userId;
        this.limit = limit;
        this.filterOutFiles = filterOutFiles;
        this.timestamp = timestamp;

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
                    queryElement = query.createElementNS(WebdavEntry.NAMESPACE_OC, "oc:favorite");
                    break;

                case RECENTLY_MODIFIED_SEARCH:
                    queryElement = query.createElementNS(DAV_NAMESPACE, "d:getlastmodified");
                    break;

                case FILE_ID_SEARCH:
                    queryElement = query.createElementNS(WebdavEntry.NAMESPACE_OC, "oc:fileid");
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

        if (searchType == SearchRemoteOperation.SearchType.PHOTO_SEARCH || searchType == SearchRemoteOperation.SearchType.RECENTLY_MODIFIED_SEARCH) {
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
        } else {
            whereElement.appendChild(equalsElement);
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
