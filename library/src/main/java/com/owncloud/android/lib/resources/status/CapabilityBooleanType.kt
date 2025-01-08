/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2018-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-FileCopyrightText: 2015 ownCloud Inc.
 * SPDX-FileCopyrightText: 2014 masensio <masensio@solidgear.es>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.status

/**
 * Enum for Boolean Type in OCCapability parameters, with values:
 * -1 - Unknown
 * 0 - False
 * 1 - True
 */
enum class CapabilityBooleanType(
    val value: Int
) {
    UNKNOWN(-1),
    FALSE(0),
    TRUE(1);

    val isUnknown: Boolean
        get() = value == -1
    val isFalse: Boolean
        get() = value == 0
    val isTrue: Boolean
        get() = value == 1

    companion object {
        @JvmStatic
        fun fromValue(value: Int): CapabilityBooleanType =
            when (value) {
                -1 -> UNKNOWN
                0 -> FALSE
                1 -> TRUE
                else -> UNKNOWN
            }

        @JvmStatic
        fun fromBooleanValue(boolValue: Boolean): CapabilityBooleanType =
            when {
                boolValue -> TRUE
                else -> FALSE
            }
    }
}
