/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2023 Tobias Kaminsky
 *   Copyright (C) 2023 Nextcloud GmbH
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

package com.owncloud.android.lib.resources.tags

import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.PutMethod
import com.owncloud.android.lib.common.operations.NextcloudRemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import okhttp3.RequestBody
import okhttp3.internal.EMPTY_REQUEST
import org.apache.commons.httpclient.HttpStatus

class PutTagRemoteOperation(val id: String, val fileId: Long) : NextcloudRemoteOperation<Void>() {
    override fun run(client: NextcloudClient): RemoteOperationResult<Void> {
        val empty: RequestBody = EMPTY_REQUEST
        val putMethod = PutMethod(
            client.baseUri.toString() + TAG_URL + fileId + "/" + id,
            true,
            empty
        )

        val status = putMethod.execute(client)

        return if (status == HttpStatus.SC_CREATED) {
            RemoteOperationResult<Void>(true, putMethod)
        } else {
            RemoteOperationResult<Void>(false, putMethod)
        }
    }

    companion object {
        const val TAG_URL = "/remote.php/dav/systemtags-relations/files/"
    }
}
