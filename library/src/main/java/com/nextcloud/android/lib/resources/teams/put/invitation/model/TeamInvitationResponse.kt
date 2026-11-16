/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.teams.put.invitation.model

enum class TeamInvitationResponse(
    val path: String
) {
    ACCEPT("/join"),
    DECLINE("/leave")
}
