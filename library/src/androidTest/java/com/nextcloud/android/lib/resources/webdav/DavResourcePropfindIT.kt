/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Your Name <your@email.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package com.nextcloud.android.lib.resources.webdav

import at.bitfire.dav4jvm.DavResource
import at.bitfire.dav4jvm.Response
import at.bitfire.dav4jvm.property.DisplayName
import at.bitfire.dav4jvm.property.ResourceType
import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.common.OwnCloudClientManagerFactory
import com.owncloud.android.lib.ocs.SEPARATOR
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Showcases dav4jvm (OkHttp-based) as an alternative to the [com.nextcloud.operations] OkHttp
 * methods: a PROPFIND with `Depth: 1` on a user's WebDAV root, reusing the same authenticated
 * OkHttpClient as [com.owncloud.android.AbstractIT.nextcloudClient].
 */
class DavResourcePropfindIT : AbstractIT() {
    @Test
    fun propfindRootFolderReturnsCollection() {
        val davResource = DavResource(buildDavHttpClient(), rootFolderUrl())

        var selfResponse: Response? = null
        davResource.propfind(PROPFIND_DEPTH, DisplayName.NAME, ResourceType.NAME) { response, relation ->
            if (relation == Response.HrefRelation.SELF) {
                selfResponse = response
            }
        }

        val resourceType = selfResponse?.get(ResourceType::class.java)
        assertTrue("PROPFIND response must include the requested collection itself", selfResponse != null)
        assertTrue(
            "root resource must be reported as a collection",
            resourceType?.types?.contains(ResourceType.COLLECTION) == true
        )
    }

    private fun buildDavHttpClient(): OkHttpClient =
        nextcloudClient.client
            .newBuilder()
            .followRedirects(false)
            .addNetworkInterceptor { chain ->
                val request =
                    chain
                        .request()
                        .newBuilder()
                        .header("Authorization", nextcloudClient.credentials)
                        .header("User-Agent", OwnCloudClientManagerFactory.getUserAgent())
                        .build()
                chain.proceed(request)
            }.build()

    private fun rootFolderUrl(): HttpUrl = "${nextcloudClient.filesDavUri}$SEPARATOR".toHttpUrl()

    companion object {
        private const val PROPFIND_DEPTH = 1
    }
}
