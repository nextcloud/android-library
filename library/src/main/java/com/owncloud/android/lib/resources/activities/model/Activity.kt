/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2017 Alejandro Bautista <aleister09@gmail.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.activities.model

import com.google.gson.annotations.SerializedName
import com.owncloud.android.lib.resources.activities.models.PreviewObject
import java.util.Date

/**
 * Activity Data Model
 */
data class Activity(
    @SerializedName("activity_id")
    val activityId: Int,
    val datetime: Date,
    // legacy purposes
    val date: Date,
    val app: String,
    val type: String,
    val user: String,
    @SerializedName("affecteduser")
    val affectedUser: String,
    val subject: String,
    val message: String,
    val icon: String,
    val link: String,
    @SerializedName("object_type")
    val objectType: String,
    @SerializedName("object_id")
    val objectId: String,
    @SerializedName("object_name")
    val objectName: String,
    val previews: List<PreviewObject>,
    @SerializedName("subject_rich")
    val richSubjectElement: RichElement
)
