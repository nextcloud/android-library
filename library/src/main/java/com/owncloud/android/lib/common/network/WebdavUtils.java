/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 TSI-mc <surinder.kumar@t-systems.com>
 * SPDX-FileCopyrightText: 2018-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2023 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro.brey@nextcloud.com>
 * SPDX-FileCopyrightText: 2018-2022 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2014-2015 ownCloud Inc.
 * SPDX-FileCopyrightText: 2015 masensio <masensio@solidgear.es>
 * SPDX-FileCopyrightText: 2014-2015 David A. Velasco <dvelasco@solidgear.es>
 * SPDX-FileCopyrightText: 2012 2012 Bartosz Przybylski <bart.p.pl@gmail.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.common.network;

import android.net.Uri;

import com.nextcloud.common.OkHttpMethodBase;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.xml.Namespace;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import androidx.annotation.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings("FS")
public class WebdavUtils {
    private static final String HTTP_DATE_FORMAT =
        "EEE, dd MMM yyyy HH:mm:ss zzz";

    private static final String SHARE_EXPIRATION_DATE_FORMAT =
            "yyyy-MM-dd HH:mm:ss";

    private static final TimeZone UTC =
            TimeZone.getTimeZone("UTC");

    private static final ThreadLocal<SimpleDateFormat> HTTP_DATE_FORMATTER =
            ThreadLocal.withInitial(() -> {
                SimpleDateFormat format =
                        new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
                format.setLenient(false);
                format.setTimeZone(UTC);
                return format;
            });

    private static final ThreadLocal<SimpleDateFormat> SHARE_EXPIRATION_FORMATTER =
            ThreadLocal.withInitial(() -> {
                SimpleDateFormat format =
                        new SimpleDateFormat(SHARE_EXPIRATION_DATE_FORMAT, Locale.US);
                format.setLenient(false);
                return format;
            });

    private static @Nullable
    Date parseStrict(SimpleDateFormat format, String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        ParsePosition position = new ParsePosition(0);
        Date parsedDate = format.parse(value, position);

        if (parsedDate == null || position.getIndex() != value.length()) {
            return null;
        }

        return parsedDate;
    }

    public static @Nullable
    Date parseResponseDate(String date) {
        return parseStrict(HTTP_DATE_FORMATTER.get(), date);
    }

    /**
     * Parses a Share API expiration date.
     *
     * <p>The value has no timezone suffix and is therefore interpreted using
     * the device default timezone.</p>
     *
     * @param date expiration date returned by the Share API
     * @return parsed date, or {@code null} if invalid
     */
    public static @Nullable
    Date parseShareExpirationDate(String date) {
        return parseStrict(SHARE_EXPIRATION_FORMATTER.get(), date);
    }

    /**
     * Encodes a path according to URI RFC 2396. 
     * 
     * If the received path doesn't start with "/", the method adds it.
     * 
     * @param remoteFilePath    Path
     * @return                  Encoded path according to RFC 2396, always starting with "/"
     */
    public static String encodePath(String remoteFilePath) {
        String encodedPath = Uri.encode(remoteFilePath, "/");
        if (!encodedPath.startsWith("/"))
            encodedPath = "/" + encodedPath;
        return encodedPath;
    }

    /**
     * Builds a DavPropertyNameSet with all prop
     * For using instead of DavConstants.PROPFIND_ALL_PROP
     * @return
     */
    public static DavPropertyNameSet getAllPropSet() {
        Namespace ocNamespace = Namespace.getNamespace(WebdavEntry.NAMESPACE_OC);
        Namespace ncNamespace = Namespace.getNamespace(WebdavEntry.NAMESPACE_NC);
        DavPropertyNameSet propSet = new DavPropertyNameSet();
        propSet.add(DavPropertyName.DISPLAYNAME);
        propSet.add(DavPropertyName.GETCONTENTTYPE);
        propSet.add(DavPropertyName.RESOURCETYPE);
        propSet.add(DavPropertyName.GETCONTENTLENGTH);
        propSet.add(DavPropertyName.GETLASTMODIFIED);
        propSet.add(DavPropertyName.CREATIONDATE);
        propSet.add(DavPropertyName.GETETAG);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_NAME_PERMISSIONS, ocNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_NAME_LOCAL_ID, ocNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_NAME_REMOTE_ID, ocNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_NAME_SIZE, ocNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_FAVORITE, ocNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_IS_ENCRYPTED, ncNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_MOUNT_TYPE, ncNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_OWNER_ID, ocNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_OWNER_DISPLAY_NAME, ocNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_UNREAD_COMMENTS, ocNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_HAS_PREVIEW, ncNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_NOTE, ncNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_SHAREES, ncNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_RICH_WORKSPACE, ncNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_CREATION_TIME, ncNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_UPLOAD_TIME, ncNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_LOCK, ncNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_LOCK_OWNER_TYPE, ncNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_LOCK_OWNER, ncNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_LOCK_OWNER_DISPLAY_NAME, ncNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_LOCK_OWNER_EDITOR, ncNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_LOCK_TIME, ncNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_LOCK_TIMEOUT, ncNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_LOCK_TOKEN, ncNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_SYSTEM_TAGS, ncNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_SYSTEM_TAGS_COLOR, ncNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_METADATA_SIZE, ncNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_METADATA_GPS, ncNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_METADATA_PHOTOS_SIZE, ncNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_METADATA_PHOTOS_GPS, ncNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_METADATA_LIVE_PHOTO, ncNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_HIDDEN, ncNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_FILE_DOWNLOAD_LIMITS, ncNamespace);

        return propSet;
    }

    /**
     * Builds a DavPropertyNameSet with properties for files
     * @return
     */
    public static DavPropertyNameSet getFilePropSet() {
        Namespace ocNamespace = Namespace.getNamespace(WebdavEntry.NAMESPACE_OC);
        Namespace ncNamespace = Namespace.getNamespace(WebdavEntry.NAMESPACE_NC);
        
        DavPropertyNameSet propSet = new DavPropertyNameSet();
        propSet.add(DavPropertyName.DISPLAYNAME);
        propSet.add(DavPropertyName.GETCONTENTTYPE);
        propSet.add(DavPropertyName.RESOURCETYPE);
        propSet.add(DavPropertyName.GETCONTENTLENGTH);
        propSet.add(DavPropertyName.GETLASTMODIFIED);
        propSet.add(DavPropertyName.CREATIONDATE);
        propSet.add(DavPropertyName.GETETAG);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_NAME_PERMISSIONS, ocNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_NAME_LOCAL_ID, ocNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_NAME_REMOTE_ID, ocNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_NAME_SIZE, ocNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_FAVORITE, ocNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_HAS_PREVIEW, ncNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_SHAREES, ncNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_CREATION_TIME, ncNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_UPLOAD_TIME, ncNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_LOCK, ncNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_LOCK_OWNER_TYPE, ncNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_LOCK_OWNER, ncNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_LOCK_OWNER_DISPLAY_NAME, ncNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_LOCK_OWNER_EDITOR, ncNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_LOCK_TIME, ncNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_LOCK_TIMEOUT, ncNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_LOCK_TOKEN, ncNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_IS_ENCRYPTED, ncNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_SYSTEM_TAGS, ncNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_METADATA_SIZE, ncNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_METADATA_GPS, ncNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_METADATA_PHOTOS_SIZE, ncNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_METADATA_PHOTOS_GPS, ncNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_METADATA_LIVE_PHOTO, ncNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_HIDDEN, ncNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_OWNER_ID, ocNamespace);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_OWNER_DISPLAY_NAME, ocNamespace);

        return propSet;
    }

    /**
     * Builds a DavPropertyNameSet with properties for trashbin
     * @return
     */
    public static DavPropertyNameSet getTrashbinPropSet() {
        DavPropertyNameSet propSet = new DavPropertyNameSet();
        propSet.add(DavPropertyName.RESOURCETYPE);
        propSet.add(DavPropertyName.GETCONTENTTYPE);
        propSet.add(DavPropertyName.GETCONTENTLENGTH);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_NAME_SIZE, Namespace.getNamespace(WebdavEntry.NAMESPACE_OC));
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_NAME_REMOTE_ID, Namespace.getNamespace(WebdavEntry.NAMESPACE_OC));
        propSet.add(WebdavEntry.TRASHBIN_FILENAME, Namespace.getNamespace(WebdavEntry.NAMESPACE_NC));
        propSet.add(WebdavEntry.TRASHBIN_ORIGINAL_LOCATION, Namespace.getNamespace(WebdavEntry.NAMESPACE_NC));
        propSet.add(WebdavEntry.TRASHBIN_DELETION_TIME, Namespace.getNamespace(WebdavEntry.NAMESPACE_NC));

        return propSet;
    }

    /**
     * Builds a DavPropertyNameSet with properties for versions
     * @return
     */
    public static DavPropertyNameSet getFileVersionPropSet() {
        DavPropertyNameSet propSet = new DavPropertyNameSet();
        propSet.add(DavPropertyName.GETCONTENTTYPE);
        propSet.add(DavPropertyName.RESOURCETYPE);
        propSet.add(DavPropertyName.GETCONTENTLENGTH);
        propSet.add(DavPropertyName.GETLASTMODIFIED);
        propSet.add(DavPropertyName.CREATIONDATE);
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_NAME_REMOTE_ID, Namespace.getNamespace(WebdavEntry.NAMESPACE_OC));
        propSet.add(WebdavEntry.EXTENDED_PROPERTY_NAME_SIZE, Namespace.getNamespace(WebdavEntry.NAMESPACE_OC));

        return propSet;
    }

    /**
     * Builds a DavPropertyNameSet with properties for chunks
     */
    public static DavPropertyNameSet getChunksPropSet() {
        DavPropertyNameSet propSet = new DavPropertyNameSet();
        propSet.add(DavPropertyName.GETCONTENTTYPE);
        propSet.add(DavPropertyName.RESOURCETYPE);
        propSet.add(DavPropertyName.GETCONTENTLENGTH);

        return propSet;
    }

    public static DavPropertyNameSet getAlbumPropSet() {
        DavPropertyNameSet propertySet = new DavPropertyNameSet();
        Namespace ncNamespace = Namespace.getNamespace("nc", WebdavEntry.NAMESPACE_NC);

        propertySet.add(DavPropertyName.create(WebdavEntry.PROPERTY_LAST_PHOTO, ncNamespace));
        propertySet.add(DavPropertyName.create(WebdavEntry.PROPERTY_NB_ITEMS, ncNamespace));
        propertySet.add(DavPropertyName.create(WebdavEntry.PROPERTY_LOCATION, ncNamespace));
        propertySet.add(DavPropertyName.create(WebdavEntry.PROPERTY_DATE_RANGE, ncNamespace));
        propertySet.add(DavPropertyName.create(WebdavEntry.PROPERTY_COLLABORATORS, ncNamespace));

        return propertySet;
    }

    /**
     * Builds a DavPropertyNameSet for the items of a photo album.
     * <p>
     * Same as {@link #getAllPropSet()} but without {@code oc:permissions}: the photos DAV endpoint cannot resolve
     * that property for album items and answers the whole PROPFIND with HTTP 500 (server side TypeError) when it
     * is requested.
     *
     * @return properties supported by the album items endpoint
     */
    public static DavPropertyNameSet getAlbumItemPropSet() {
        DavPropertyNameSet propSet = new DavPropertyNameSet(getAllPropSet());
        propSet.remove(DavPropertyName.create(WebdavEntry.EXTENDED_PROPERTY_NAME_PERMISSIONS,
                                              Namespace.getNamespace(WebdavEntry.NAMESPACE_OC)));

        return propSet;
    }

    /**
     *
     * @param rawEtag
     * @return
     */
    public static String parseEtag(String rawEtag) {
        if (rawEtag == null || rawEtag.length() == 0) {
            return "";
        }
        if (rawEtag.endsWith("-gzip")) {
            rawEtag = rawEtag.substring(0, rawEtag.length() - 5);
        }
        if (rawEtag.length() >= 2 && rawEtag.startsWith("\"") && rawEtag.endsWith("\"")) {
            rawEtag = rawEtag.substring(1, rawEtag.length() - 1);
        }
        return rawEtag;
    }

    public static String getEtagFromResponse(HttpMethod method) {
        Header eTag = method.getResponseHeader("OC-ETag");
        if (eTag == null) {
            eTag = method.getResponseHeader("oc-etag");
        }
        if (eTag == null) {
            eTag = method.getResponseHeader("ETag");
        }
        if (eTag == null) {
            eTag = method.getResponseHeader("etag");
        }
        String result = "";
        if (eTag != null) {
            result = parseEtag(eTag.getValue());
        }
        return result;
    }

    public static String getEtagFromResponse(OkHttpMethodBase method) {
        String eTag = method.getResponseHeader("OC-ETag");
        if (eTag == null) {
            eTag = method.getResponseHeader("oc-etag");
        }
        if (eTag == null) {
            eTag = method.getResponseHeader("ETag");
        }
        if (eTag == null) {
            eTag = method.getResponseHeader("etag");
        }
        String result = "";
        if (eTag != null) {
            result = parseEtag(eTag);
        }
        return result;
    }
}
