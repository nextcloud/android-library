/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2016-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2023 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-FileCopyrightText: 2015 ownCloud Inc.
 * SPDX-License-Identifier: MIT
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

    var assistant: Boolean = false

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
    var endToEndEncryptionApiVersion = E2EVersion.UNKNOWN

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

    // Groupfolders
    var groupfolders = CapabilityBooleanType.UNKNOWN

    // Drop-Account
    var dropAccount = CapabilityBooleanType.UNKNOWN

    // Security guard
    var securityGuard = CapabilityBooleanType.UNKNOWN

    // Etag for capabilities
    var etag: String? = ""

    val version: OwnCloudVersion
        get() =
            OwnCloudVersion(
                listOf(versionMayor, versionMinor, versionMicro).joinToString(
                    VERSION_DOT
                )
            )

    companion object {
        private const val VERSION_DOT = "."
    }
}
