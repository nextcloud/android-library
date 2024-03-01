/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2022-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.common

import android.net.Uri

interface NextcloudUriProvider {
    /**
     * Root URI of the Nextcloud server
     */
    var baseUri: Uri?
    val filesDavUri: Uri
    val uploadUri: Uri
    val davUri: Uri

    fun getFilesDavUri(path: String): String

    fun getCommentsUri(fileId: Long): String
}
