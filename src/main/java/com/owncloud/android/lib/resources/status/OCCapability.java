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

/**
 * Contains data of the Capabilities for an account, from the Capabilities API
 */
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
    private CapabilityBooleanType filesFileDrop;

    private CapabilityBooleanType supportsNotificationsV1;
    private CapabilityBooleanType supportsNotificationsV2;

    private CapabilityBooleanType externalLinks;

    // Fullnextsearch
    private CapabilityBooleanType fullNextSearchEnabled;
    private CapabilityBooleanType fullNextSearchFiles;

    private CapabilityBooleanType endToEndEncryption;

    // Richdocuments
    private CapabilityBooleanType richdocuments;
    private CapabilityBooleanType richdocumentsDirectEditing;
    private CapabilityBooleanType richdocumentsTemplatesAvailable;
    private List<String> richdocumentsMimeTypeList;

    private CapabilityBooleanType activity;

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
        serverTextColor = "";
        serverLogo = "";
        serverBackground = "";
        serverBackgroundDefault = CapabilityBooleanType.UNKNOWN;
        serverBackgroundPlain = CapabilityBooleanType.UNKNOWN;

        corePollInterval = 0;

        filesSharingApiEnabled = CapabilityBooleanType.UNKNOWN;
        filesSharingPublicEnabled = CapabilityBooleanType.UNKNOWN;
        filesSharingPublicPasswordEnforced = CapabilityBooleanType.UNKNOWN;
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
        filesFileDrop = CapabilityBooleanType.UNKNOWN;

        supportsNotificationsV1 = CapabilityBooleanType.UNKNOWN;
        supportsNotificationsV2 = CapabilityBooleanType.UNKNOWN;

        externalLinks = CapabilityBooleanType.UNKNOWN;

        endToEndEncryption = CapabilityBooleanType.UNKNOWN;

        activity = CapabilityBooleanType.UNKNOWN;

        richdocuments = CapabilityBooleanType.UNKNOWN;
        richdocumentsMimeTypeList = new ArrayList<>();
        richdocumentsDirectEditing = CapabilityBooleanType.FALSE;
        richdocumentsTemplatesAvailable = CapabilityBooleanType.FALSE;
    }


    public CapabilityBooleanType getSupportsNotificationsV1() {
        return supportsNotificationsV1;
    }

    public void setSupportsNotificationsV1(CapabilityBooleanType mSupportsNotificationsV1) {
        this.supportsNotificationsV1 = mSupportsNotificationsV1;
    }

    public CapabilityBooleanType getSupportsNotificationsV2() {
        return supportsNotificationsV2;
    }

    public void setSupportsNotificationsV2(CapabilityBooleanType mSupportsNotificationsV2) {
        this.supportsNotificationsV2 = mSupportsNotificationsV2;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getVersionMayor() {
        return versionMayor;
    }

    public void setVersionMayor(int versionMayor) {
        this.versionMayor = versionMayor;
    }

    public int getVersionMinor() {
        return versionMinor;
    }

    public void setVersionMinor(int versionMinor) {
        this.versionMinor = versionMinor;
    }

    public int getVersionMicro() {
        return versionMicro;
    }

    public void setVersionMicro(int versionMicro) {
        this.versionMicro = versionMicro;
    }

    public String getVersionString() {
        return versionString;
    }

    public void setVersionString(String versionString) {
        this.versionString = versionString;
    }

    public OwnCloudVersion getVersion() {
        return new OwnCloudVersion(versionMayor + VERSION_DOT + versionMinor + VERSION_DOT + versionMicro);
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }


    public String getServerBackground() {
        return serverBackground;
    }

    public void setServerBackground(String mServerBackground) {
        this.serverBackground = mServerBackground;
    }

    public String getServerLogo() {
        return serverLogo;
    }

    public void setServerLogo(String mServerLogo) {
        this.serverLogo = mServerLogo;
    }

    public String getServerColor() {
        return serverColor;
    }

    public void setServerColor(String mServerColor) {
        this.serverColor = mServerColor;
    }

    public String getServerTextColor() {
        return serverTextColor;
    }

    public void setServerTextColor(String mServerTextColor) {
        this.serverTextColor = mServerTextColor;
    }

    public String getServerElementColor() {
        return serverElementColor;
    }

    public void setServerElementColor(String mServerElementColor) {
        this.serverElementColor = mServerElementColor;
    }

    public String getServerSlogan() {
        return serverSlogan;
    }

    public void setServerSlogan(String mServerSlogan) {
        this.serverSlogan = mServerSlogan;
    }

    public CapabilityBooleanType getServerBackgroundPlain() {
        return serverBackgroundPlain;
    }

    public void setServerBackgroundPlain(CapabilityBooleanType serverBackgroundPlain) {
        this.serverBackgroundPlain = serverBackgroundPlain;
    }

    public CapabilityBooleanType getServerBackgroundDefault() {
        return serverBackgroundDefault;
    }

    public void setServerBackgroundDefault(CapabilityBooleanType serverBackgroundDefault) {
        this.serverBackgroundDefault = serverBackgroundDefault;
    }

    public String getVersionEdition() {
        return versionEdition;
    }

    public void setVersionEdition(String versionEdition) {
        this.versionEdition = versionEdition;
    }

    public int getCorePollInterval() {
        return corePollInterval;
    }

    public void setCorePollInterval(int corePollInterval) {
        this.corePollInterval = corePollInterval;
    }

    public CapabilityBooleanType getFilesSharingApiEnabled() {
        return filesSharingApiEnabled;
    }

    public void setFilesSharingApiEnabled(CapabilityBooleanType filesSharingApiEnabled) {
        this.filesSharingApiEnabled = filesSharingApiEnabled;
    }

    public CapabilityBooleanType getFilesSharingPublicEnabled() {
        return filesSharingPublicEnabled;
    }

    public void setFilesSharingPublicEnabled(CapabilityBooleanType filesSharingPublicEnabled) {
        this.filesSharingPublicEnabled = filesSharingPublicEnabled;
    }

    public CapabilityBooleanType getFilesSharingPublicPasswordEnforced() {
        return filesSharingPublicPasswordEnforced;
    }

    public void setFilesSharingPublicPasswordEnforced(CapabilityBooleanType filesSharingPublicPasswordEnforced) {
        this.filesSharingPublicPasswordEnforced = filesSharingPublicPasswordEnforced;
    }

    public CapabilityBooleanType getFilesSharingPublicExpireDateEnabled() {
        return filesSharingPublicExpireDateEnabled;
    }

    public void setFilesSharingPublicExpireDateEnabled(CapabilityBooleanType filesSharingPublicExpireDateEnabled) {
        this.filesSharingPublicExpireDateEnabled = filesSharingPublicExpireDateEnabled;
    }

    public int getFilesSharingPublicExpireDateDays() {
        return filesSharingPublicExpireDateDays;
    }

    public void setFilesSharingPublicExpireDateDays(int filesSharingPublicExpireDateDays) {
        this.filesSharingPublicExpireDateDays = filesSharingPublicExpireDateDays;
    }

    public CapabilityBooleanType getFilesSharingPublicExpireDateEnforced() {
        return filesSharingPublicExpireDateEnforced;
    }

    public void setFilesSharingPublicExpireDateEnforced(CapabilityBooleanType filesSharingPublicExpireDateEnforced) {
        this.filesSharingPublicExpireDateEnforced = filesSharingPublicExpireDateEnforced;
    }

    public CapabilityBooleanType getFilesSharingPublicSendMail() {
        return filesSharingPublicSendMail;
    }

    public void setFilesSharingPublicSendMail(CapabilityBooleanType filesSharingPublicSendMail) {
        this.filesSharingPublicSendMail = filesSharingPublicSendMail;
    }

    public CapabilityBooleanType getFilesSharingPublicUpload() {
        return filesSharingPublicUpload;
    }

    public void setFilesSharingPublicUpload(CapabilityBooleanType filesSharingPublicUpload) {
        this.filesSharingPublicUpload = filesSharingPublicUpload;
    }

    public CapabilityBooleanType getFilesSharingUserSendMail() {
        return filesSharingUserSendMail;
    }

    public void setFilesSharingUserSendMail(CapabilityBooleanType filesSharingUserSendMail) {
        this.filesSharingUserSendMail = filesSharingUserSendMail;
    }

    public CapabilityBooleanType getFilesSharingResharing() {
        return filesSharingResharing;
    }

    public void setFilesSharingResharing(CapabilityBooleanType filesSharingResharing) {
        this.filesSharingResharing = filesSharingResharing;
    }

    public CapabilityBooleanType getFilesSharingFederationOutgoing() {
        return filesSharingFederationOutgoing;
    }

    public void setFilesSharingFederationOutgoing(CapabilityBooleanType filesSharingFederationOutgoing) {
        this.filesSharingFederationOutgoing = filesSharingFederationOutgoing;
    }

    public CapabilityBooleanType getFilesSharingFederationIncoming() {
        return filesSharingFederationIncoming;
    }

    public void setFilesSharingFederationIncoming(CapabilityBooleanType filesSharingFederationIncoming) {
        this.filesSharingFederationIncoming = filesSharingFederationIncoming;
    }

    public CapabilityBooleanType getFilesBigFileChuncking() {
        return filesBigFileChunking;
    }

    public void setFilesBigFileChuncking(CapabilityBooleanType filesBigFileChuncking) {
        this.filesBigFileChunking = filesBigFileChuncking;
    }

    public CapabilityBooleanType getFilesUndelete() {
        return filesUndelete;
    }

    public void setFilesUndelete(CapabilityBooleanType filesUndelete) {
        this.filesUndelete = filesUndelete;
    }

    public CapabilityBooleanType getFilesVersioning() {
        return filesVersioning;
    }

    public void setFilesVersioning(CapabilityBooleanType filesVersioning) {
        this.filesVersioning = filesVersioning;
    }

    public CapabilityBooleanType getFilesFileDrop() {
        return filesFileDrop;
    }

    public void setFilesFileDrop(CapabilityBooleanType mFilesFileDrop) {
        this.filesFileDrop = mFilesFileDrop;
    }

    public CapabilityBooleanType getExternalLinks() {
        return externalLinks;
    }

    public void setExternalLinks(CapabilityBooleanType mExternalLinks) {
        this.externalLinks = mExternalLinks;
    }

    public CapabilityBooleanType getFullNextSearchEnabled() {
        return fullNextSearchEnabled;
    }

    public void setFullNextSearchEnabled(CapabilityBooleanType fullNextSearchEnabled) {
        this.fullNextSearchEnabled = fullNextSearchEnabled;
    }

    public CapabilityBooleanType getFullNextSearchFiles() {
        return fullNextSearchFiles;
    }

    public void setFullNextSearchFiles(CapabilityBooleanType fullNextSearchFiles) {
        this.fullNextSearchFiles = fullNextSearchFiles;
    }

    public CapabilityBooleanType getEndToEndEncryption() {
        return endToEndEncryption;
    }

    public void setEndToEndEncryption(CapabilityBooleanType endToEndEncryption) {
        this.endToEndEncryption = endToEndEncryption;
    }

    public CapabilityBooleanType isActivityEnabled() {
        return activity;
    }

    public void setActivity(CapabilityBooleanType mActivity) {
        this.activity = mActivity;
    }

    public CapabilityBooleanType getRichDocuments() {
        return richdocuments;
    }

    public void setRichDocuments(CapabilityBooleanType richdocuments) {
        this.richdocuments = richdocuments;
    }

    public List<String> getRichDocumentsMimeTypeList() {
        return richdocumentsMimeTypeList;
    }

    public void setRichDocumentsMimeTypeList(List<String> mimeTypeList) {
        richdocumentsMimeTypeList = mimeTypeList;
    }

    public void setRichDocumentsDirectEditing(CapabilityBooleanType directEditing) {
        richdocumentsDirectEditing = directEditing;
    }

    public CapabilityBooleanType getRichDocumentsDirectEditing() {
        return richdocumentsDirectEditing;
    }

    public CapabilityBooleanType getRichdocumentsTemplatesAvailable() {
        return richdocumentsTemplatesAvailable;
    }

    public void setRichdocumentsTemplatesAvailable(CapabilityBooleanType richdocumentsTemplatesAvailable) {
        this.richdocumentsTemplatesAvailable = richdocumentsTemplatesAvailable;
    }
}
