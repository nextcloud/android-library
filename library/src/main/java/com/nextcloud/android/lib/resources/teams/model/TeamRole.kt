/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.teams.model

private const val LEVEL_MODERATOR = 4
private const val LEVEL_ADMIN = 8
private const val LEVEL_OWNER = 9

enum class TeamRole {
    OWNER,
    ADMIN,
    MODERATOR,
    MEMBER;

    companion object {
        fun fromLevel(level: Int): TeamRole =
            when {
                level >= LEVEL_OWNER -> OWNER
                level >= LEVEL_ADMIN -> ADMIN
                level >= LEVEL_MODERATOR -> MODERATOR
                else -> MEMBER
            }
    }
}
