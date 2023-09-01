/*
 *
 * Nextcloud Android client application
 *
 * @author Tobias Kaminsky
 * Copyright (C) 2023 Tobias Kaminsky
 * Copyright (C) 2023 Nextcloud GmbH
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
package com.nextcloud.operations

import android.net.Uri
import at.bitfire.dav4jvm.DavResource
import at.bitfire.dav4jvm.Response
import com.nextcloud.common.DavMethod
import com.owncloud.android.lib.common.network.WebdavUtils
import com.owncloud.android.lib.common.utils.WebDavFileUtils
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import org.apache.jackrabbit.webdav.DavConstants

class PropFindMethod(httpUrl: HttpUrl) : DavMethod<PropFindResult>(httpUrl) {
    override fun apply(client: OkHttpClient, httpUrl: HttpUrl, filesDavUri: Uri): PropFindResult {
        val webDavFileUtils = WebDavFileUtils()
        val result = PropFindResult()

        DavResource(client, httpUrl)
            .propfind(
                DavConstants.DEPTH_1,
                *WebdavUtils.getAllPropertiesList()
            ) { response: Response, hrefRelation: Response.HrefRelation? ->
                result.success = response.isSuccess()

                when (hrefRelation) {
                    Response.HrefRelation.MEMBER -> result.children.add(
                        webDavFileUtils.parseResponse(response, filesDavUri)
                    )

                    Response.HrefRelation.SELF -> result.root =
                        webDavFileUtils.parseResponse(response, filesDavUri)

                    Response.HrefRelation.OTHER -> {}
                    else -> {}
                }
            }

        return result
    }
} 
