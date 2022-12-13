/*
 *
 * Nextcloud Android client application
 *
 * @author Tobias Kaminsky
 * Copyright (C) 2020 Tobias Kaminsky
 * Copyright (C) 2020 Nextcloud GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
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
        val nextcloud_26 = NextcloudVersion(0x1A000000) // 25.0
    }

    constructor(string: String) : super(string)
    constructor(version: Int) : super(version)
}
