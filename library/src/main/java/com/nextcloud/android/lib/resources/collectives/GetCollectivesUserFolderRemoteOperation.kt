/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.collectives

import com.nextcloud.android.lib.resources.collectives.model.CollectivesUserFolder
import com.nextcloud.operations.GetMethod
import com.nextcloud.operations.OcsKotlinRemoteOperation

class GetCollectivesUserFolderRemoteOperation : OcsKotlinRemoteOperation<CollectivesUserFolder>() {
    override fun createMethod(baseUrl: String) = GetMethod(baseUrl + ENDPOINT + JSON_FORMAT, true)

    override fun parse(body: String) = decode(body, CollectivesUserFolder.serializer())

    companion object {
        private const val ENDPOINT = "/ocs/v2.php/apps/collectives/api/v1.0/settings/user/user_folder"
    }
}
