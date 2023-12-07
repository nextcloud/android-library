/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2020 Tobias Kaminsky
 *   Copyright (C) 2020 Nextcloud GmbH
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
    }

    constructor(string: String) : super(string)
    constructor(version: Int) : super(version)
}
