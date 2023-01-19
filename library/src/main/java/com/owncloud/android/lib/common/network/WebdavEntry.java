/* ownCloud Android Library is available under MIT license
 *   Copyright (C) 2015 ownCloud Inc.
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

package com.owncloud.android.lib.common.network;

import android.net.Uri;

import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.resources.files.model.FileLockType;
import com.owncloud.android.lib.resources.shares.ShareType;
import com.owncloud.android.lib.resources.shares.ShareeUser;

import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.xml.Namespace;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import lombok.Setter;

public class WebdavEntry {

    private static final String TAG = WebdavEntry.class.getSimpleName();

    public static final String NAMESPACE_OC = "http://owncloud.org/ns";
    public static final String NAMESPACE_NC = "http://nextcloud.org/ns";
    public static final String EXTENDED_PROPERTY_NAME_PERMISSIONS = "permissions";
    public static final String EXTENDED_PROPERTY_NAME_LOCAL_ID = "fileid";
    public static final String EXTENDED_PROPERTY_NAME_REMOTE_ID = "id";
    public static final String EXTENDED_PROPERTY_NAME_SIZE = "size";
    public static final String EXTENDED_PROPERTY_FAVORITE = "favorite";
    public static final String EXTENDED_PROPERTY_IS_ENCRYPTED = "is-encrypted";
    public static final String EXTENDED_PROPERTY_MOUNT_TYPE = "mount-type";
    public static final String EXTENDED_PROPERTY_OWNER_ID = "owner-id";
    public static final String EXTENDED_PROPERTY_OWNER_DISPLAY_NAME = "owner-display-name";
    public static final String EXTENDED_PROPERTY_UNREAD_COMMENTS = "comments-unread";
    public static final String EXTENDED_PROPERTY_HAS_PREVIEW = "has-preview";
    public static final String EXTENDED_PROPERTY_NOTE = "note";
    public static final String EXTENDED_PROPERTY_SHAREES = "sharees";
    public static final String EXTENDED_PROPERTY_RICH_WORKSPACE = "rich-workspace";
    public static final String EXTENDED_PROPERTY_CREATION_TIME = "creation_time";
    public static final String EXTENDED_PROPERTY_UPLOAD_TIME = "upload_time";
    public static final String EXTENDED_PROPERTY_LOCK = "lock";
    public static final String EXTENDED_PROPERTY_LOCK_OWNER_TYPE = "lock-owner-type";
    public static final String EXTENDED_PROPERTY_LOCK_OWNER = "lock-owner";
    public static final String EXTENDED_PROPERTY_LOCK_OWNER_DISPLAY_NAME = "lock-owner-displayname";
    public static final String EXTENDED_PROPERTY_LOCK_OWNER_EDITOR = "lock-owner-editor";
    public static final String EXTENDED_PROPERTY_LOCK_TIME = "lock-time";
    public static final String EXTENDED_PROPERTY_LOCK_TIMEOUT = "lock-timeout";
    public static final String EXTENDED_PROPERTY_LOCK_TOKEN = "lock-token";

    public static final String TRASHBIN_FILENAME = "trashbin-filename";
    public static final String TRASHBIN_ORIGINAL_LOCATION = "trashbin-original-location";
    public static final String TRASHBIN_DELETION_TIME = "trashbin-deletion-time";
    public static final String SHAREES_DISPLAY_NAME = "display-name";
    public static final String SHAREES_ID = "id";
    public static final String SHAREES_SHARE_TYPE = "type";

    public static final String PROPERTY_QUOTA_USED_BYTES = "quota-used-bytes";
    public static final String PROPERTY_QUOTA_AVAILABLE_BYTES = "quota-available-bytes";

    private static final String IS_ENCRYPTED = "1";

    private static final int CODE_PROP_NOT_FOUND = 404;

    @Getter private String name;
    @Getter private String path;
    @Getter private String uri;
    @Getter private String contentType;
    @Getter private String eTag;
    @Getter private String permissions;
    @Getter
    private String remoteId;
    @Getter
    private long localId;
    @Getter
    private String trashbinOriginalLocation;
    @Getter private String trashbinFilename;
    @Getter private long trashbinDeletionTimestamp;
    @Getter @Setter private boolean favorite;
    @Getter private boolean encrypted;
    @Getter private MountType mountType;
    @Getter private long contentLength;
    @Getter private long createTimestamp;
    @Getter
    private long modifiedTimestamp;
    @Getter
    private long uploadTimestamp;
    @Getter
    private long size;
    @Getter private BigDecimal quotaUsedBytes;
    @Getter private BigDecimal quotaAvailableBytes;
    @Getter private String ownerId;
    @Getter @Setter private String ownerDisplayName;
    @Getter private int unreadCommentsCount;
    @Getter @Setter private boolean hasPreview;
    @Getter private String note = "";
    @Getter private ShareeUser[] sharees = new ShareeUser[0];
    @Getter private String richWorkspace = null;
    @Getter private boolean isLocked = false;
    @Getter
    private FileLockType lockOwnerType = null;
    @Getter
    private String lockOwnerId = null;
    @Getter
    private String lockOwnerDisplayName = null;
    @Getter
    private long lockTimestamp;
    @Getter
    private String lockOwnerEditor = null;
    @Getter
    private long lockTimeout;
    @Getter
    private String lockToken = null;

    public enum MountType {INTERNAL, EXTERNAL, GROUP}

    @SuppressFBWarnings(
            value = "STT_TOSTRING_STORED_IN_FIELD",
            justification = "Will be replaced with davX5")
    public WebdavEntry(MultiStatusResponse ms, String splitElement) {
        resetData();

        Namespace ocNamespace = Namespace.getNamespace(NAMESPACE_OC);
        Namespace ncNamespace = Namespace.getNamespace(NAMESPACE_NC);

        if (ms.getStatus().length != 0) {
            uri = ms.getHref();

            path = uri.split(splitElement, 2)[1].replace("//", "/");

            int status = ms.getStatus()[0].getStatusCode();
            if ( status == CODE_PROP_NOT_FOUND ) {
                status = ms.getStatus()[1].getStatusCode();
            }
            DavPropertySet propSet = ms.getProperties(status);
            @SuppressWarnings("rawtypes")
            DavProperty prop = propSet.get(DavPropertyName.DISPLAYNAME);
            if (prop != null) {
                name = prop.getName().toString();
                name = name.substring(1, name.length() - 1);
            } else {
                String[] tmp = path.split("/");
                if (tmp.length > 0)
                    name = tmp[tmp.length - 1];
            }

            // use unknown mimetype as default behavior
            // {DAV:}getcontenttype
            contentType = "application/octet-stream";
            prop = propSet.get(DavPropertyName.GETCONTENTTYPE);
            if (prop != null) {
                String contentType = (String) prop.getValue();
                // dvelasco: some builds of ownCloud server 4.0.x added a trailing ';'
                // to the MIME type ; if looks fixed, but let's be cautious
                if (contentType != null) {
                    if (contentType.contains(";")) {
                        this.contentType = contentType.substring(0, contentType.indexOf(";"));
                    } else {
                        this.contentType = contentType;
                    }
                }
            }

            // check if it's a folder in the standard way: see RFC2518 12.2 . RFC4918 14.3
            // {DAV:}resourcetype
            prop = propSet.get(DavPropertyName.RESOURCETYPE);
            if (prop!= null) {
                Object value = prop.getValue();
                if (value != null) {
                    contentType = "DIR";   // a specific attribute would be better,
                    // but this is enough;
                    // unless while we have no reason to distinguish
                    // MIME types for folders
                }
            }

            // {DAV:}getcontentlength
            prop = propSet.get(DavPropertyName.GETCONTENTLENGTH);
            if (prop != null) {
                contentLength = Long.parseLong((String) prop.getValue());
            }

            // {DAV:}getlastmodified
            prop = propSet.get(DavPropertyName.GETLASTMODIFIED);
            if (prop != null) {
                Date d = WebdavUtils.parseResponseDate((String) prop.getValue());
                modifiedTimestamp = (d != null) ? d.getTime() : 0;
            }

            // {NS:} creation_time
            prop = propSet.get(EXTENDED_PROPERTY_CREATION_TIME, ncNamespace);
            if (prop != null) {
                try {
                    createTimestamp = Long.parseLong((String) prop.getValue());
                } catch (NumberFormatException e) {
                    createTimestamp = 0;
                }
            }

            // {NS:} upload_time
            prop = propSet.get(EXTENDED_PROPERTY_UPLOAD_TIME, ncNamespace);
            if (prop != null) {
                try {
                    uploadTimestamp = Long.parseLong((String) prop.getValue());
                } catch (NumberFormatException e) {
                    uploadTimestamp = 0;
                }
            }

            // {DAV:}getetag
            prop = propSet.get(DavPropertyName.GETETAG);
            if (prop != null) {
                eTag = (String) prop.getValue();
                eTag = WebdavUtils.parseEtag(eTag);
            }

            // {DAV:}quota-used-bytes
            prop = propSet.get(DavPropertyName.create(PROPERTY_QUOTA_USED_BYTES));
            if (prop != null) {
                String quotaUsedBytesSt = (String) prop.getValue();
                try {
                    quotaUsedBytes = new BigDecimal(quotaUsedBytesSt);
                } catch (NumberFormatException e) {
                    Log_OC.w(TAG, "No value for QuotaUsedBytes - NumberFormatException");
                } catch (NullPointerException e ){
                    Log_OC.w(TAG, "No value for QuotaUsedBytes - NullPointerException");
                }
                Log_OC.d(TAG , "QUOTA_USED_BYTES " + quotaUsedBytesSt );
            }

            // {DAV:}quota-available-bytes
            prop = propSet.get(DavPropertyName.create(PROPERTY_QUOTA_AVAILABLE_BYTES));
            if (prop != null) {
                String quotaAvailableBytesSt = (String) prop.getValue();
                try {
                    quotaAvailableBytes = new BigDecimal(quotaAvailableBytesSt);
                } catch (NumberFormatException e) {
                    Log_OC.w(TAG, "No value for QuotaAvailableBytes - NumberFormatException");
                } catch (NullPointerException e ){
                    Log_OC.w(TAG, "No value for QuotaAvailableBytes");
                }
                Log_OC.d(TAG , "QUOTA_AVAILABLE_BYTES " + quotaAvailableBytesSt );
            }

            // OC permissions property <oc:permissions>
            prop = propSet.get(EXTENDED_PROPERTY_NAME_PERMISSIONS, ocNamespace);
            if (prop != null && prop.getValue() != null) {
                permissions = prop.getValue().toString();
            }

            // OC remote id property <oc:id>
            prop = propSet.get(EXTENDED_PROPERTY_NAME_REMOTE_ID, ocNamespace);
            if (prop != null) {
                remoteId = prop.getValue().toString();
            }

            // OC remote id property <oc:fileid>
            prop = propSet.get(EXTENDED_PROPERTY_NAME_LOCAL_ID, ocNamespace);
            if (prop != null) {
                localId = Long.parseLong((String) prop.getValue());
            }

            // OC size property <oc:size>
            prop = propSet.get(EXTENDED_PROPERTY_NAME_SIZE, ocNamespace);
            if (prop != null) {
                size = Long.parseLong((String) prop.getValue());
            }

            // OC favorite property <oc:favorite>
            prop = propSet.get(EXTENDED_PROPERTY_FAVORITE, ocNamespace);
            if (prop != null) {
                String favoriteValue = (String) prop.getValue();
                favorite = IS_ENCRYPTED.equals(favoriteValue);
            } else {
                favorite = false;
            }

            // NC encrypted property <nc:is-encrypted>
            prop = propSet.get(EXTENDED_PROPERTY_IS_ENCRYPTED, ncNamespace);
            if (prop != null) {
                String encryptedValue = (String) prop.getValue();
                encrypted = IS_ENCRYPTED.equals(encryptedValue);
            } else {
                encrypted = false;
            }

            // NC mount-type property <nc:mount-type>
            prop = propSet.get(EXTENDED_PROPERTY_MOUNT_TYPE, ncNamespace);
            if (prop != null) {
                if ("external".equals(prop.getValue())) {
                    mountType = MountType.EXTERNAL;
                } else if ("group".equals(prop.getValue())) {
                    mountType = MountType.GROUP;
                } else {
                    mountType = MountType.INTERNAL;
                }
            } else {
                mountType = MountType.INTERNAL;
            }

            // OC owner-id property <oc:owner-id>
            prop = propSet.get(EXTENDED_PROPERTY_OWNER_ID, ocNamespace);
            if (prop != null) {
                ownerId = (String) prop.getValue();
            } else {
                ownerId = "";
            }

            // OC owner-display-name property <oc:owner-display-name>
            prop = propSet.get(EXTENDED_PROPERTY_OWNER_DISPLAY_NAME, ocNamespace);
            if (prop != null) {
                ownerDisplayName = (String) prop.getValue();
            } else {
                ownerDisplayName = "";
            }

            // OC unread comments property <oc-comments-unread>
            prop = propSet.get(EXTENDED_PROPERTY_UNREAD_COMMENTS, ocNamespace);
            if (prop != null) {
                unreadCommentsCount = Integer.valueOf(prop.getValue().toString());
            } else {
                unreadCommentsCount = 0;
            }

            // NC has preview property <nc-has-preview>
            prop = propSet.get(EXTENDED_PROPERTY_HAS_PREVIEW, ncNamespace);
            if (prop != null) {
                hasPreview = Boolean.valueOf(prop.getValue().toString());
            } else {
                hasPreview = true;
            }

            // NC trashbin-original-location <nc:trashbin-original-location>
            prop = propSet.get(TRASHBIN_ORIGINAL_LOCATION, ncNamespace);
            if (prop != null) {
                trashbinOriginalLocation = prop.getValue().toString();
            }

            // NC trashbin-filename <nc:trashbin-filename>
            prop = propSet.get(TRASHBIN_FILENAME, ncNamespace);
            if (prop != null) {
                trashbinFilename = prop.getValue().toString();
            }

            // NC trashbin-deletion-time <nc:trashbin-deletion-time>
            prop = propSet.get(TRASHBIN_DELETION_TIME, ncNamespace);
            if (prop != null) {
                trashbinDeletionTimestamp = Long.parseLong((String) prop.getValue());
            }

            // NC note property <nc:note>
            prop = propSet.get(EXTENDED_PROPERTY_NOTE, ncNamespace);
            if (prop != null && prop.getValue() != null) {
                note = prop.getValue().toString();
            }

            // NC rich-workspace property <nc:rich-workspace>
            // can be null if rich-workspace is disabled for this user
            prop = propSet.get(EXTENDED_PROPERTY_RICH_WORKSPACE, ncNamespace);
            if (prop != null) {
                if (prop.getValue() != null) {
                    richWorkspace = prop.getValue().toString();
                } else {
                    richWorkspace = "";
                }
            } else {
                richWorkspace = null;
            }

            // NC sharees property <nc-sharees>
            prop = propSet.get(EXTENDED_PROPERTY_SHAREES, ncNamespace);
            if (prop != null && prop.getValue() != null) {
                if (prop.getValue() instanceof ArrayList) {
                    ArrayList list = (ArrayList) prop.getValue();

                    List<ShareeUser> tempList = new ArrayList<>();

                    for (int i = 0; i < list.size(); i++) {
                        Element element = (Element) list.get(i);

                        ShareeUser user = createShareeUser(element);

                        if (user != null) {
                            tempList.add(user);
                        }
                    }

                    sharees = tempList.toArray(new ShareeUser[0]);

                } else {
                    // single item or empty
                    Element element = (Element) prop.getValue();

                    ShareeUser user = createShareeUser(element);

                    if (user != null) {
                        sharees = new ShareeUser[]{user};
                    }
                }
            }

            parseLockProperties(ncNamespace, propSet);


        } else {
            Log_OC.e("WebdavEntry", "General error, no status for webdav response");
        }
    }

    private void parseLockProperties(Namespace ncNamespace, DavPropertySet propSet) {
        DavProperty<?> prop;
        // file locking
        prop = propSet.get(EXTENDED_PROPERTY_LOCK, ncNamespace);
        if (prop != null && prop.getValue() != null) {
            isLocked = "1".equals((String) prop.getValue());
        } else {
            isLocked = false;
        }

        prop = propSet.get(EXTENDED_PROPERTY_LOCK_OWNER_TYPE, ncNamespace);
        if (prop != null && prop.getValue() != null) {
            final int value = Integer.parseInt((String) prop.getValue());
            lockOwnerType = FileLockType.fromValue(value);
        } else {
            lockOwnerType = null;
        }

        lockOwnerId = parseStringProp(propSet, EXTENDED_PROPERTY_LOCK_OWNER, ncNamespace);
        lockOwnerDisplayName = parseStringProp(propSet, EXTENDED_PROPERTY_LOCK_OWNER_DISPLAY_NAME, ncNamespace);
        lockOwnerEditor = parseStringProp(propSet, EXTENDED_PROPERTY_LOCK_OWNER_EDITOR, ncNamespace);
        lockTimestamp = parseLongProp(propSet, EXTENDED_PROPERTY_LOCK_TIME, ncNamespace);
        lockTimeout = parseLongProp(propSet, EXTENDED_PROPERTY_LOCK_TIMEOUT, ncNamespace);
        lockToken = parseStringProp(propSet, EXTENDED_PROPERTY_LOCK_TOKEN, ncNamespace);
    }

    private String parseStringProp(final DavPropertySet propSet, final String propName, final Namespace namespace) {
        final DavProperty<?> prop = propSet.get(propName, namespace);
        if (prop != null && prop.getValue() != null) {
            return (String) prop.getValue();
        } else {
            return null;
        }
    }

    private Long parseLongProp(final DavPropertySet propSet, final String propName, final Namespace namespace) {
        final String stringValue = parseStringProp(propSet, propName, namespace);
        if (stringValue != null) {
            return Long.parseLong(stringValue);
        } else {
            return 0L;
        }
    }

    private @Nullable
    ShareeUser createShareeUser(Element element) {
        String displayName = extractDisplayName(element);
        String userId = extractUserId(element);
        ShareType shareType = extractShareType(element);

        if ((ShareType.EMAIL == shareType ||
                ShareType.FEDERATED == shareType ||
                ShareType.GROUP == shareType ||
                ShareType.ROOM == shareType ||
                !displayName.isEmpty())
                && !userId.isEmpty()) {
            return new ShareeUser(userId, displayName, shareType);
        } else {
            return null;
        }
    }
    
    private String extractDisplayName(Element element) {
        Node displayName = element.getElementsByTagNameNS(NAMESPACE_NC, SHAREES_DISPLAY_NAME).item(0);
        if (displayName != null && displayName.getFirstChild() != null) {
            return displayName.getFirstChild().getNodeValue();
        }

        return "";
    }

    private String extractUserId(Element element) {
        Node userId = element.getElementsByTagNameNS(NAMESPACE_NC, SHAREES_ID).item(0);
        if (userId != null && userId.getFirstChild() != null) {
            return userId.getFirstChild().getNodeValue();
        }

        return "";
    }

    private ShareType extractShareType(Element element) {
        Node shareType = element.getElementsByTagNameNS(NAMESPACE_NC, SHAREES_SHARE_TYPE).item(0);
        if (shareType != null && shareType.getFirstChild() != null) {
            int value = Integer.parseInt(shareType.getFirstChild().getNodeValue());

            return ShareType.fromValue(value);
        }

        return ShareType.NO_SHARED;
    }
    
    public String decodedPath() {
        return Uri.decode(path);
    }

    public boolean isDirectory() {
        return "DIR".equals(contentType);
    }

    private void resetData() {
        name = uri = contentType = permissions = null;
        remoteId = null;
        localId = -1;
        contentLength = createTimestamp = modifiedTimestamp = 0;
        size = 0;
        quotaUsedBytes = null;
        quotaAvailableBytes = null;
        favorite = false;
        hasPreview = false;
    }
}
