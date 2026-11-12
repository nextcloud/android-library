/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.teams.get.team

import com.nextcloud.android.lib.resources.teams.model.Team
import com.nextcloud.operations.GetMethod
import com.nextcloud.operations.OcsKotlinRemoteOperation
import kotlinx.serialization.builtins.ListSerializer

class GetTeamsRemoteOperation : OcsKotlinRemoteOperation<List<Team>>() {
    override fun createMethod(baseUrl: String) = GetMethod(baseUrl + ENDPOINT + JSON_FORMAT, true)

    override fun parse(body: String) = decode(body, ListSerializer(Team.serializer()))

    companion object {
        private const val ENDPOINT = "/ocs/v2.php/apps/circles/circles"
    }
}
