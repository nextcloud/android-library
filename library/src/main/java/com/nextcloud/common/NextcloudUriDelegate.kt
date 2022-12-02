/*
 *  Nextcloud Android Library is available under MIT license
 *
 *  @author Álvaro Brey Vilas
 *  Copyright (C) 2022 Álvaro Brey Vilas
 *  Copyright (C) 2022 Nextcloud GmbH
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 *  BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 *  ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package com.nextcloud.common

import android.net.Uri
import com.owncloud.android.lib.common.accounts.AccountUtils
import com.owncloud.android.lib.common.network.WebdavUtils

/**
 * Transitory class to share uri logic between [com.owncloud.android.lib.common.OwnCloudClient]
 * and [com.nextcloud.common.NextcloudClient].
 *
 * When finally getting rid of [com.owncloud.android.lib.common.OwnCloudClient],
 * this should be separate from the client.
 */
class NextcloudUriDelegate(baseUri: Uri, var userId: String?) : NextcloudUriProvider {

    constructor(baseUri: Uri) : this(baseUri, null)

    val userIdEncoded: String?
        get() = userId?.let { UserIdEncoder.encode(it) }

    /**
     * Root URI of the Nextcloud server
     */
    override var baseUri: Uri? = baseUri
        set(uri) {
            requireNotNull(uri) { "URI cannot be NULL" }
            field = uri
        }

    override val filesDavUri: Uri
        get() = Uri.parse("$davUri/files/$userIdEncoded")
    override val uploadUri: Uri
        get() = Uri.parse(baseUri.toString() + AccountUtils.DAV_UPLOAD)
    override val davUri: Uri
        get() = Uri.parse(baseUri.toString() + AccountUtils.WEBDAV_PATH_9_0)

    override fun getFilesDavUri(path: String): String {
        // encodePath already adds leading slash if needed
        return "$filesDavUri${WebdavUtils.encodePath(path)}"
    }

    override fun getCommentsUri(fileId: Long): String {
        return "$davUri/comments/files/$fileId"
    }
}
