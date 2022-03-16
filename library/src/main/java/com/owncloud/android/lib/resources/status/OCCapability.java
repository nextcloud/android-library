/* ownCloud Android Library is available under MIT license
 *   @author masensio
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
package com.owncloud.android.lib.resources.status;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Contains data of the Capabilities for an account, from the Capabilities API
 */
@Getter
@Setter
public class OCCapability {
    private static final String VERSION_DOT = ".";

    private long id;
    private String accountName;

    // Server version
    private int versionMayor;
    private int versionMinor;
    private int versionMicro;
    private String versionString;
    private String versionEdition;

    // Theming
    private String serverName;
    private String serverSlogan;
    private String serverColor;
    private String serverTextColor;
    private String serverElementColor;
    private String serverElementColorBright;
    private String serverElementColorDark;
    private String serverLogo;
    private String serverBackground;
    private CapabilityBooleanType serverBackgroundDefault;
    private CapabilityBooleanType serverBackgroundPlain;

    // Core PollInterval
    private int corePollInterval;

    // Files Sharing
    private CapabilityBooleanType filesSharingApiEnabled;
    private CapabilityBooleanType filesSharingPublicEnabled;
    private CapabilityBooleanType filesSharingPublicPasswordEnforced;
    private CapabilityBooleanType filesSharingPublicAskForOptionalPassword;
    private CapabilityBooleanType filesSharingPublicExpireDateEnabled;
    private int filesSharingPublicExpireDateDays;
    private CapabilityBooleanType filesSharingPublicExpireDateEnforced;
    private CapabilityBooleanType filesSharingPublicSendMail;
    private CapabilityBooleanType filesSharingPublicUpload;
    private CapabilityBooleanType filesSharingUserSendMail;
    private CapabilityBooleanType filesSharingResharing;
    private CapabilityBooleanType filesSharingFederationOutgoing;
    private CapabilityBooleanType filesSharingFederationIncoming;

    // Files
    private CapabilityBooleanType filesBigFileChunking;
    private CapabilityBooleanType filesUndelete;
    private CapabilityBooleanType filesVersioning;

    private CapabilityBooleanType supportsNotificationsV1;
    private CapabilityBooleanType supportsNotificationsV2;

    private CapabilityBooleanType externalLinks;

    // Fullnextsearch
    private CapabilityBooleanType fullNextSearchEnabled;
    private CapabilityBooleanType fullNextSearchFiles;

    private CapabilityBooleanType endToEndEncryption;

    // Richdocuments
    private CapabilityBooleanType richDocuments;
    private CapabilityBooleanType richDocumentsDirectEditing;
    private CapabilityBooleanType richDocumentsTemplatesAvailable;
    private List<String> richDocumentsMimeTypeList;
    private List<String> richDocumentsOptionalMimeTypeList;
    private String richDocumentsProductName;

    private CapabilityBooleanType activity;

    private CapabilityBooleanType extendedSupport;

    // DirectEditing
    private String directEditingEtag;

    // user status
    private CapabilityBooleanType userStatus;
    private CapabilityBooleanType userStatusSupportsEmoji;

    // Etag for capabilities
    private String etag;

    public OCCapability() {
        id = 0;
        accountName = "";

        versionMayor = 0;
        versionMinor = 0;
        versionMicro = 0;
        versionString = "";
        serverName = "";
        serverSlogan = "";
        serverColor = "";
        serverElementColor = "";
        serverElementColorBright = "";
        serverElementColorDark = "";
        serverTextColor = "";
        serverLogo = "";
        serverBackground = "";
        serverBackgroundDefault = CapabilityBooleanType.UNKNOWN;
        serverBackgroundPlain = CapabilityBooleanType.UNKNOWN;

        corePollInterval = 0;

        filesSharingApiEnabled = CapabilityBooleanType.UNKNOWN;
        filesSharingPublicEnabled = CapabilityBooleanType.UNKNOWN;
        filesSharingPublicPasswordEnforced = CapabilityBooleanType.UNKNOWN;
        filesSharingPublicAskForOptionalPassword = CapabilityBooleanType.UNKNOWN;
        filesSharingPublicExpireDateEnabled = CapabilityBooleanType.UNKNOWN;
        filesSharingPublicExpireDateDays = 0;
        filesSharingPublicExpireDateEnforced = CapabilityBooleanType.UNKNOWN;
        filesSharingPublicSendMail = CapabilityBooleanType.UNKNOWN;
        filesSharingPublicUpload = CapabilityBooleanType.UNKNOWN;
        filesSharingUserSendMail = CapabilityBooleanType.UNKNOWN;
        filesSharingResharing = CapabilityBooleanType.UNKNOWN;
        filesSharingFederationOutgoing = CapabilityBooleanType.UNKNOWN;
        filesSharingFederationIncoming = CapabilityBooleanType.UNKNOWN;

        filesBigFileChunking = CapabilityBooleanType.UNKNOWN;
        filesUndelete = CapabilityBooleanType.UNKNOWN;
        filesVersioning = CapabilityBooleanType.UNKNOWN;

        supportsNotificationsV1 = CapabilityBooleanType.UNKNOWN;
        supportsNotificationsV2 = CapabilityBooleanType.UNKNOWN;

        externalLinks = CapabilityBooleanType.UNKNOWN;

        endToEndEncryption = CapabilityBooleanType.UNKNOWN;

        activity = CapabilityBooleanType.UNKNOWN;

        richDocuments = CapabilityBooleanType.UNKNOWN;
        richDocumentsMimeTypeList = new ArrayList<>();
        richDocumentsOptionalMimeTypeList = new ArrayList<>();
        richDocumentsDirectEditing = CapabilityBooleanType.FALSE;
        richDocumentsTemplatesAvailable = CapabilityBooleanType.FALSE;
        richDocumentsProductName = "Collabora Online";

        extendedSupport = CapabilityBooleanType.UNKNOWN;

        userStatus = CapabilityBooleanType.UNKNOWN;
        userStatusSupportsEmoji = CapabilityBooleanType.UNKNOWN;

        directEditingEtag = "";
        etag = "";
    }

    public OwnCloudVersion getVersion() {
        return new OwnCloudVersion(versionMayor + VERSION_DOT + versionMinor + VERSION_DOT + versionMicro);
    }
}
