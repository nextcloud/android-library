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
package com.owncloud.android.lib.resources.status

/**
 * Contains data of the Capabilities for an account, from the Capabilities API
 */
class OCCapability {
    var id: Long = 0
    var accountName: String? = ""

    // Server version
    var versionMayor = 0
    var versionMinor = 0
    var versionMicro = 0
    var versionString: String? = ""
    var versionEdition: String? = null

    // Theming
    var serverName: String? = ""
    var serverSlogan: String? = ""
    var serverColor: String? = ""
    var serverTextColor: String? = ""
    var serverElementColor: String? = ""
    var serverElementColorBright: String? = ""
    var serverElementColorDark: String? = ""
    var serverLogo: String? = ""
    var serverBackground: String? = ""
    var serverBackgroundDefault = CapabilityBooleanType.UNKNOWN
    var serverBackgroundPlain = CapabilityBooleanType.UNKNOWN

    // Core PollInterval
    var corePollInterval: Int = 0

    // Files Sharing
    var filesSharingApiEnabled = CapabilityBooleanType.UNKNOWN
    var filesSharingPublicEnabled = CapabilityBooleanType.UNKNOWN
    var filesSharingPublicPasswordEnforced = CapabilityBooleanType.UNKNOWN
    var filesSharingPublicAskForOptionalPassword = CapabilityBooleanType.UNKNOWN
    var filesSharingPublicExpireDateEnabled = CapabilityBooleanType.UNKNOWN
    var filesSharingPublicExpireDateDays: Int = 0
    var filesSharingPublicExpireDateEnforced = CapabilityBooleanType.UNKNOWN
    var filesSharingPublicSendMail = CapabilityBooleanType.UNKNOWN
    var filesSharingPublicUpload = CapabilityBooleanType.UNKNOWN
    var filesSharingUserSendMail = CapabilityBooleanType.UNKNOWN
    var filesSharingResharing = CapabilityBooleanType.UNKNOWN
    var filesSharingFederationOutgoing = CapabilityBooleanType.UNKNOWN
    var filesSharingFederationIncoming = CapabilityBooleanType.UNKNOWN

    // Files
    var filesBigFileChunking = CapabilityBooleanType.UNKNOWN
    var filesUndelete = CapabilityBooleanType.UNKNOWN
    var filesVersioning = CapabilityBooleanType.UNKNOWN

    var filesLockingVersion: String? = null

    var supportsNotificationsV1 = CapabilityBooleanType.UNKNOWN
    var supportsNotificationsV2 = CapabilityBooleanType.UNKNOWN

    var externalLinks = CapabilityBooleanType.UNKNOWN

    // Fullnextsearch
    var fullNextSearchEnabled: CapabilityBooleanType? = null
    var fullNextSearchFiles: CapabilityBooleanType? = null

    var endToEndEncryption = CapabilityBooleanType.UNKNOWN
    var endToEndEncryptionKeysExist = CapabilityBooleanType.UNKNOWN

    // Richdocuments
    var richDocuments = CapabilityBooleanType.UNKNOWN
    var richDocumentsDirectEditing = CapabilityBooleanType.FALSE
    var richDocumentsTemplatesAvailable = CapabilityBooleanType.FALSE
    var richDocumentsMimeTypeList: List<String>? = emptyList()
    var richDocumentsOptionalMimeTypeList: List<String>? = emptyList()
    var richDocumentsProductName: String? = "Collabora Online"

    var activity = CapabilityBooleanType.UNKNOWN

    var extendedSupport = CapabilityBooleanType.UNKNOWN

    // DirectEditing
    var directEditingEtag: String? = ""

    // user status
    var userStatus = CapabilityBooleanType.UNKNOWN
    var userStatusSupportsEmoji = CapabilityBooleanType.UNKNOWN

    // Etag for capabilities
    var etag: String? = ""

    val version: OwnCloudVersion
        get() = OwnCloudVersion(listOf(versionMayor, versionMinor, versionMicro).joinToString(VERSION_DOT))

    companion object {
        private const val VERSION_DOT = "."
    }
}
