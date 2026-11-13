/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.teams.get.resources

import com.nextcloud.android.lib.resources.teams.get.resources.model.TeamResource
import com.nextcloud.android.lib.resources.teams.get.resources.model.TeamResourcesResponse
import com.nextcloud.operations.GetMethod
import com.nextcloud.operations.OcsKotlinRemoteOperation
import com.owncloud.android.lib.ocs.SEPARATOR

class GetTeamResourcesRemoteOperation(
    private val teamId: String
) : OcsKotlinRemoteOperation<List<TeamResource>>() {
    override fun createMethod(baseUrl: String) =
        GetMethod(baseUrl + ENDPOINT + SEPARATOR + teamId + RESOURCES + JSON_FORMAT, true)

    override fun parse(body: String) = decode(body, TeamResourcesResponse.serializer()).resources

    companion object {
        private const val ENDPOINT = "/ocs/v2.php/teams"
        private const val RESOURCES = "/resources"
    }
}
