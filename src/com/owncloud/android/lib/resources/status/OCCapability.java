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

/**
 * Contains data of the Capabilities for an account, from the Capabilities API
 */
public class OCCapability {

    private static final String TAG = OCCapability.class.getSimpleName();

    private long mId;
    private String mAccountName;

    // Server version
    private int mVersionMayor;
    private int mVersionMinor;
    private int mVersionMicro;
    private String mVersionString;
    private String mVersionEdition;
    
    // Theming
    private String mServerName;
    private String mServerSlogan;
    private String mServerColor;
    private String mServerTextColor;
    private String mServerElementColor;
    private String mServerLogo;
    private String mServerBackground;
    private CapabilityBooleanType mServerBackgroundDefault;
    private CapabilityBooleanType mServerBackgroundPlain;

    // Core PollInterval
    private int mCorePollinterval;

    // Files Sharing
    private CapabilityBooleanType mFilesSharingApiEnabled;

    private CapabilityBooleanType mFilesSharingPublicEnabled;
    private CapabilityBooleanType mFilesSharingPublicPasswordEnforced;
    private CapabilityBooleanType mFilesSharingPublicExpireDateEnabled;
    private int mFilesSharingPublicExpireDateDays;
    private CapabilityBooleanType mFilesSharingPublicExpireDateEnforced;
    private CapabilityBooleanType mFilesSharingPublicSendMail;
    private CapabilityBooleanType mFilesSharingPublicUpload;

    private CapabilityBooleanType mFilesSharingUserSendMail;

    private CapabilityBooleanType mFilesSharingResharing;

    private CapabilityBooleanType mFilesSharingFederationOutgoing;
    private CapabilityBooleanType mFilesSharingFederationIncoming;

    // Files
    private CapabilityBooleanType mFilesBigFileChuncking;
    private CapabilityBooleanType mFilesUndelete;
    private CapabilityBooleanType mFilesVersioning;
    private CapabilityBooleanType mFilesFileDrop;

    private CapabilityBooleanType mSupportsNotificationsV1;
    private CapabilityBooleanType mSupportsNotificationsV2;

    private CapabilityBooleanType mExternalLinks;
    
    // Fullnextsearch
    private CapabilityBooleanType mFullNextSearchEnabled;
    private CapabilityBooleanType mFullNextSearchFiles;
    

    private CapabilityBooleanType mEndToEndEncryption;

    public OCCapability(){
        mId = 0;
        mAccountName = "";

        mVersionMayor = 0;
        mVersionMinor = 0;
        mVersionMicro = 0;
        mVersionString = "";
        mVersionString = "";
        mServerName = "";
        mServerSlogan = "";
        mServerColor = "";
        mServerElementColor = "";
        mServerTextColor = "";
        mServerLogo = "";
        mServerBackground = "";
        mServerBackgroundDefault = CapabilityBooleanType.UNKNOWN;
        mServerBackgroundPlain = CapabilityBooleanType.UNKNOWN;

        mCorePollinterval = 0;

        mFilesSharingApiEnabled = CapabilityBooleanType.UNKNOWN;
        mFilesSharingPublicEnabled = CapabilityBooleanType.UNKNOWN;
        mFilesSharingPublicPasswordEnforced = CapabilityBooleanType.UNKNOWN;
        mFilesSharingPublicExpireDateEnabled = CapabilityBooleanType.UNKNOWN;
        mFilesSharingPublicExpireDateDays = 0;
        mFilesSharingPublicExpireDateEnforced = CapabilityBooleanType.UNKNOWN;
        mFilesSharingPublicSendMail = CapabilityBooleanType.UNKNOWN;
        mFilesSharingPublicUpload = CapabilityBooleanType.UNKNOWN;
        mFilesSharingUserSendMail = CapabilityBooleanType.UNKNOWN;
        mFilesSharingResharing = CapabilityBooleanType.UNKNOWN;
        mFilesSharingFederationOutgoing = CapabilityBooleanType.UNKNOWN;
        mFilesSharingFederationIncoming = CapabilityBooleanType.UNKNOWN;

        mFilesBigFileChuncking = CapabilityBooleanType.UNKNOWN;
        mFilesUndelete = CapabilityBooleanType.UNKNOWN;
        mFilesVersioning = CapabilityBooleanType.UNKNOWN;
        mFilesFileDrop = CapabilityBooleanType.UNKNOWN;

        mSupportsNotificationsV1 = CapabilityBooleanType.UNKNOWN;
        mSupportsNotificationsV2 = CapabilityBooleanType.UNKNOWN;

        mExternalLinks = CapabilityBooleanType.UNKNOWN;

        mEndToEndEncryption = CapabilityBooleanType.UNKNOWN;
    }


    // Getters and Setters


    public CapabilityBooleanType getSupportsNotificationsV1() {
        return mSupportsNotificationsV1;
    }

    public void setSupportsNotificationsV1(CapabilityBooleanType mSupportsNotificationsV1) {
        this.mSupportsNotificationsV1 = mSupportsNotificationsV1;
    }

    public CapabilityBooleanType getSupportsNotificationsV2() {
        return mSupportsNotificationsV2;
    }

    public void setSupportsNotificationsV2(CapabilityBooleanType mSupportsNotificationsV2) {
        this.mSupportsNotificationsV2 = mSupportsNotificationsV2;
    }

    public String getAccountName() {
        return mAccountName;
    }

    public void setAccountName(String accountName) {
        this.mAccountName = accountName;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        this.mId = id;
    }

    public int getVersionMayor() {
        return mVersionMayor;
    }

    public void setVersionMayor(int versionMayor) {
        this.mVersionMayor = versionMayor;
    }

    public int getVersionMinor() {
        return mVersionMinor;
    }

    public void setVersionMinor(int versionMinor) {
        this.mVersionMinor = versionMinor;
    }

    public int getVersionMicro() {
        return mVersionMicro;
    }

    public void setVersionMicro(int versionMicro) {
        this.mVersionMicro = versionMicro;
    }

    public String getVersionString() {
        return mVersionString;
    }

    public void setVersionString(String versionString) {
        this.mVersionString = versionString;
    }

    public String getServerName() {
        return mServerName;
    }

    public void setServerName(String serverName) {
        this.mServerName = serverName;
    }


    public String getServerBackground() {
        return mServerBackground;
    }

    public void setServerBackground(String mServerBackground) {
        this.mServerBackground = mServerBackground;
    }

    public String getServerLogo() {
        return mServerLogo;
    }

    public void setServerLogo(String mServerLogo) {
        this.mServerLogo = mServerLogo;
    }

    public String getServerColor() {
        return mServerColor;
    }

    public void setServerColor(String mServerColor) {
        this.mServerColor = mServerColor;
    }

    public String getServerTextColor() {
        return mServerTextColor;
    }

    public void setServerTextColor(String mServerTextColor) {
        this.mServerTextColor = mServerTextColor;
    }

    public String getServerElementColor() {
        return mServerElementColor;
    }

    public void setServerElementColor(String mServerElementColor) {
        this.mServerElementColor = mServerElementColor;
    }

    public String getServerSlogan() {
        return mServerSlogan;
    }

    public void setServerSlogan(String mServerSlogan) {
        this.mServerSlogan = mServerSlogan;
    }

    public CapabilityBooleanType getServerBackgroundPlain() {
        return mServerBackgroundPlain;
    }

    public void setServerBackgroundPlain(CapabilityBooleanType serverBackgroundPlain) {
        this.mServerBackgroundPlain = serverBackgroundPlain;
    }

    public CapabilityBooleanType getServerBackgroundDefault() {
        return mServerBackgroundDefault;
    }

    public void setServerBackgroundDefault(CapabilityBooleanType serverBackgroundDefault) {
        this.mServerBackgroundDefault = serverBackgroundDefault;
    }

    public String getVersionEdition() {
        return mVersionEdition;
    }

    public void setVersionEdition(String versionEdition) {
        this.mVersionEdition = versionEdition;
    }
    
    public int getCorePollinterval() {
        return mCorePollinterval;
    }

    public void setCorePollinterval(int corePollinterval) {
        this.mCorePollinterval = corePollinterval;
    }

    public CapabilityBooleanType getFilesSharingApiEnabled() {
        return mFilesSharingApiEnabled;
    }

    public void setFilesSharingApiEnabled(CapabilityBooleanType filesSharingApiEnabled) {
        this.mFilesSharingApiEnabled = filesSharingApiEnabled;
    }

    public CapabilityBooleanType getFilesSharingPublicEnabled() {
        return mFilesSharingPublicEnabled;
    }

    public void setFilesSharingPublicEnabled(CapabilityBooleanType filesSharingPublicEnabled) {
        this.mFilesSharingPublicEnabled = filesSharingPublicEnabled;
    }

    public CapabilityBooleanType getFilesSharingPublicPasswordEnforced() {
        return mFilesSharingPublicPasswordEnforced;
    }

    public void setFilesSharingPublicPasswordEnforced(CapabilityBooleanType filesSharingPublicPasswordEnforced) {
        this.mFilesSharingPublicPasswordEnforced = filesSharingPublicPasswordEnforced;
    }

    public CapabilityBooleanType getFilesSharingPublicExpireDateEnabled() {
        return mFilesSharingPublicExpireDateEnabled;
    }

    public void setFilesSharingPublicExpireDateEnabled(CapabilityBooleanType filesSharingPublicExpireDateEnabled) {
        this.mFilesSharingPublicExpireDateEnabled = filesSharingPublicExpireDateEnabled;
    }

    public int getFilesSharingPublicExpireDateDays() {
        return mFilesSharingPublicExpireDateDays;
    }

    public void setFilesSharingPublicExpireDateDays(int filesSharingPublicExpireDateDays) {
        this.mFilesSharingPublicExpireDateDays = filesSharingPublicExpireDateDays;
    }

    public CapabilityBooleanType getFilesSharingPublicExpireDateEnforced() {
        return mFilesSharingPublicExpireDateEnforced;
    }

    public void setFilesSharingPublicExpireDateEnforced(CapabilityBooleanType filesSharingPublicExpireDateEnforced) {
        this.mFilesSharingPublicExpireDateEnforced = filesSharingPublicExpireDateEnforced;
    }
    
    public CapabilityBooleanType getFilesSharingPublicSendMail() {
        return mFilesSharingPublicSendMail;
    }

    public void setFilesSharingPublicSendMail(CapabilityBooleanType filesSharingPublicSendMail) {
        this.mFilesSharingPublicSendMail = filesSharingPublicSendMail;
    }

    public CapabilityBooleanType getFilesSharingPublicUpload() {
        return mFilesSharingPublicUpload;
    }

    public void setFilesSharingPublicUpload(CapabilityBooleanType filesSharingPublicUpload) {
        this.mFilesSharingPublicUpload = filesSharingPublicUpload;
    }

    public CapabilityBooleanType getFilesSharingUserSendMail() {
        return mFilesSharingUserSendMail;
    }

    public void setFilesSharingUserSendMail(CapabilityBooleanType filesSharingUserSendMail) {
        this.mFilesSharingUserSendMail = filesSharingUserSendMail;
    }

    public CapabilityBooleanType getFilesSharingResharing() {
        return mFilesSharingResharing;
    }

    public void setFilesSharingResharing(CapabilityBooleanType filesSharingResharing) {
        this.mFilesSharingResharing = filesSharingResharing;
    }
    
    public CapabilityBooleanType getFilesSharingFederationOutgoing() {
        return mFilesSharingFederationOutgoing;
    }

    public void setFilesSharingFederationOutgoing(CapabilityBooleanType filesSharingFederationOutgoing) {
        this.mFilesSharingFederationOutgoing = filesSharingFederationOutgoing;
    }

    public CapabilityBooleanType getFilesSharingFederationIncoming() {
        return mFilesSharingFederationIncoming;
    }

    public void setFilesSharingFederationIncoming(CapabilityBooleanType filesSharingFederationIncoming) {
        this.mFilesSharingFederationIncoming = filesSharingFederationIncoming;
    }

    public CapabilityBooleanType getFilesBigFileChuncking() {
        return mFilesBigFileChuncking;
    }

    public void setFilesBigFileChuncking(CapabilityBooleanType filesBigFileChuncking) {
        this.mFilesBigFileChuncking = filesBigFileChuncking;
    }

    public CapabilityBooleanType getFilesUndelete() {
        return mFilesUndelete;
    }

    public void setFilesUndelete(CapabilityBooleanType filesUndelete) {
        this.mFilesUndelete = filesUndelete;
    }

    public CapabilityBooleanType getFilesVersioning() {
        return mFilesVersioning;
    }

    public void setFilesVersioning(CapabilityBooleanType filesVersioning) {
        this.mFilesVersioning = filesVersioning;
    }

    public CapabilityBooleanType getFilesFileDrop() {
        return mFilesFileDrop;
    }

    public void setFilesFileDrop(CapabilityBooleanType mFilesFileDrop) {
        this.mFilesFileDrop = mFilesFileDrop;
    }

    public CapabilityBooleanType getExternalLinks() {
        return mExternalLinks;
    }

    public void setExternalLinks(CapabilityBooleanType mExternalLinks) {
        this.mExternalLinks = mExternalLinks;
    }

    public CapabilityBooleanType getFullNextSearchEnabled() {
        return mFullNextSearchEnabled;
    }

    public void setFullNextSearchEnabled(CapabilityBooleanType fullNextSearchEnabled) {
        mFullNextSearchEnabled = fullNextSearchEnabled;
    }

    public CapabilityBooleanType getFullNextSearchFiles() {
        return mFullNextSearchFiles;
    }

    public void setFullNextSearchFiles(CapabilityBooleanType fullNextSearchFiles) {
        mFullNextSearchFiles = fullNextSearchFiles;
    }
    public CapabilityBooleanType getEndToEndEncryption() {
        return mEndToEndEncryption;
    }

    public void setEndToEndEncryption(CapabilityBooleanType endToEndEncryption) {
        this.mEndToEndEncryption = endToEndEncryption;
    }
}
