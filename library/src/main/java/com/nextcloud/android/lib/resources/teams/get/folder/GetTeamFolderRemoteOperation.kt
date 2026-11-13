/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.teams.get.folder

import com.nextcloud.android.lib.resources.teams.get.folder.model.TeamFolder
import com.nextcloud.operations.GetMethod
import com.nextcloud.operations.OcsKotlinRemoteOperation
import com.owncloud.android.lib.ocs.SEPARATOR

class GetTeamFolderRemoteOperation(
    private val teamId: String
) : OcsKotlinRemoteOperation<TeamFolder>() {
    override fun createMethod(baseUrl: String) =
        GetMethod(baseUrl + ENDPOINT + SEPARATOR + teamId + FOLDER + JSON_FORMAT, true)

    override fun parse(body: String) = decode(body, TeamFolder.serializer())

    companion object {
        private const val ENDPOINT = "/ocs/v2.php/apps/circles/teams"
        private const val FOLDER = "/folder"
    }
}
