/* Nextcloud Android Library is available under MIT license
*
* @author Tobias Kaminsky
* Copyright (C) 2023 Tobias Kaminsky
* Copyright (C) 2023 Nextcloud GmbH
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

package com.nextcloud.operations

import android.net.Uri
import at.bitfire.dav4jvm.DavResource
import at.bitfire.dav4jvm.Property
import at.bitfire.dav4jvm.Response
import com.nextcloud.common.DavMethod
import com.owncloud.android.lib.common.network.WebdavUtils
import com.owncloud.android.lib.common.utils.WebDavFileUtils
import okhttp3.HttpUrl
import okhttp3.OkHttpClient

class PropFindMethod
@JvmOverloads constructor(
    httpUrl: HttpUrl,
    private val propertySet: Array<Property.Name> = WebdavUtils.PROPERTYSETS.ALL,
    private val depth: Int = 1
) : DavMethod<PropFindResult>(httpUrl) {

    override fun apply(client: OkHttpClient, httpUrl: HttpUrl, filesDavUri: Uri): PropFindResult {
        val result = PropFindResult()

        DavResource(client, httpUrl).propfind(
            depth, *propertySet
        ) { response: Response, hrefRelation: Response.HrefRelation? ->
            result.davResponse.success = response.isSuccess()
            response.status?.let { status ->
                result.davResponse.status = status
            }


            when (hrefRelation) {
                Response.HrefRelation.MEMBER -> result.children.add(
                    WebDavFileUtils.parseResponse(response, filesDavUri)
                )

                Response.HrefRelation.SELF, Response.HrefRelation.OTHER -> result.root =
                    WebDavFileUtils.parseResponse(response, filesDavUri)

                else -> {}
            }
        }

        return result
    }
} 
