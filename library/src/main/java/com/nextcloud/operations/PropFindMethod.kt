/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2023-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2024 ZetaTom <70907959+ZetaTom@users.noreply.github.com>
 * SPDX-FileCopyrightText: 2023 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
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
    @JvmOverloads
    constructor(
        httpUrl: HttpUrl,
        private val propertySet: Array<Property.Name> = WebdavUtils.PROPERTYSETS.ALL,
        private val depth: Int = 1
    ) : DavMethod<PropFindResult>(httpUrl) {
        override fun apply(
            client: OkHttpClient,
            httpUrl: HttpUrl,
            filesDavUri: Uri
        ): PropFindResult {
            val result = PropFindResult()

            @Suppress("SpreadOperator")
            DavResource(client, httpUrl).propfind(
                depth,
                *propertySet
            ) { response: Response, hrefRelation: Response.HrefRelation? ->
                result.davResponse.success = response.isSuccess()
                response.status?.let { status ->
                    result.davResponse.status = status
                }

                when (hrefRelation) {
                    Response.HrefRelation.MEMBER ->
                        result.children.add(
                            WebDavFileUtils.parseResponse(response, filesDavUri)
                        )

                    Response.HrefRelation.SELF, Response.HrefRelation.OTHER ->
                        result.root =
                            WebDavFileUtils.parseResponse(response, filesDavUri)

                    else -> {}
                }
            }

            return result
        }
    } 
