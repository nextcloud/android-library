/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2024 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.assistant.model

import com.google.gson.annotations.SerializedName

enum class TaskIds(val id: String) {
    GenerateText("core:text2text"),
    ExtractTopics("core:text2text:topics"),
    GenerateHeadline("core:text2text:headline"),
    Summarize("core:text2text:summary"),
}

data class TaskTypes(val types: TaskType)

data class TaskType(
    @SerializedName("core:text2text")
    val generateText: GenerateText,
    @SerializedName("core:text2text:topics")
    val extractTopics: ExtractTopics,
    @SerializedName("core:text2text:headline")
    val generateHeadline: GenerateHeadline,
    @SerializedName("core:text2text:summary")
    val summarize: Summarize
)

data class GenerateText(val name: String, val description: String)
data class ExtractTopics(val name: String, val description: String)
data class GenerateHeadline(val name: String, val description: String)
data class Summarize(val name: String, val description: String)
