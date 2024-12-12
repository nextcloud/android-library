/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2020 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.status

class NextcloudVersion : OwnCloudVersion {
    companion object {
        @JvmField
        val nextcloud_21 = NextcloudVersion(0x15000000) // 21.0

        @JvmField
        val nextcloud_22 = NextcloudVersion(0x16000000) // 22.0

        @JvmField
        val nextcloud_23 = NextcloudVersion(0x17000000) // 23.0

        @JvmField
        val nextcloud_24 = NextcloudVersion(0x18000000) // 24.0

        @JvmField
        val nextcloud_25 = NextcloudVersion(0x19000000) // 25.0

        @JvmField
        val nextcloud_26 = NextcloudVersion(0x1A000000) // 26.0

        @JvmField
        val nextcloud_27 = NextcloudVersion(0x1B000000) // 27.0

        @JvmField
        val nextcloud_28 = NextcloudVersion(0x1C000000) // 28.0

        @JvmField
        val nextcloud_29 = NextcloudVersion(0x1D000000) // 29.0

        @JvmField
        val nextcloud_30 = NextcloudVersion(0x1E000000) // 30.0
        
        @JvmField
        val nextcloud_31 = NextcloudVersion(0x1F000000) // 31.0
    }

    constructor(string: String) : super(string)
    constructor(version: Int) : super(version)
}
