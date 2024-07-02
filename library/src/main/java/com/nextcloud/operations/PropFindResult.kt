/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2023-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2024 ZetaTom <70907959+ZetaTom@users.noreply.github.com>
 * SPDX-FileCopyrightText: 2023 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.operations

import com.nextcloud.common.DavResponse
import com.owncloud.android.lib.resources.files.model.RemoteFile

data class PropFindResult(
    val davResponse: DavResponse = DavResponse(),
    var root: RemoteFile = RemoteFile(),
    val children: MutableList<RemoteFile> = mutableListOf()
) {
    fun getContent(): List<RemoteFile> {
        return listOf(root) + children
    }
}
