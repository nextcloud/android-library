/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.teams.model

import com.owncloud.android.lib.resources.shares.ShareType

private const val TYPE_USER = 1
private const val TYPE_GROUP = 2
private const val TYPE_MAIL = 4
private const val TYPE_CONTACT = 8
private const val TYPE_CIRCLE = 16

enum class TeamMemberType(
    val value: Int
) {
    USER(TYPE_USER),
    GROUP(TYPE_GROUP),
    MAIL(TYPE_MAIL),
    CONTACT(TYPE_CONTACT),
    CIRCLE(TYPE_CIRCLE);

    companion object {
        fun fromValue(value: Int): TeamMemberType? = entries.firstOrNull { it.value == value }

        fun fromShareType(shareType: ShareType): TeamMemberType? =
            when (shareType) {
                ShareType.USER, ShareType.FEDERATED -> USER
                ShareType.GROUP -> GROUP
                ShareType.EMAIL -> MAIL
                ShareType.CIRCLE -> CIRCLE
                else -> null
            }
    }
}
