/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2024 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.assistant.model

import com.google.gson.annotations.SerializedName

data class TaskTypes(val types: TaskType)

data class TaskType(
    @SerializedName("core:text2text")
    val coreText2text: CoreText2text,
    @SerializedName("core:text2text:topics")
    val coreText2textTopics: CoreText2textTopics,
    @SerializedName("core:text2text:headline")
    val coreText2textHeadline: CoreText2textHeadline,
    @SerializedName("core:text2text:summary")
    val coreText2textSummary: CoreText2textSummary,
    @SerializedName("core:text2text:translate")
    val coreText2textTranslate: CoreText2textTranslate,
    @SerializedName("core:contextwrite")
    val coreContextwrite: CoreContextwrite,
    @SerializedName("context_chat:context_chat")
    val contextChatContextChat: ContextChatContextChat
)

data class CoreText2text(val name: String, val description: String)
data class CoreText2textTopics(val name: String, val description: String)
data class CoreText2textHeadline(val name: String, val description: String)
data class CoreText2textSummary(val name: String, val description: String)
data class CoreText2textTranslate(val name: String, val description: String)
data class CoreContextwrite(val name: String, val description: String)
data class ContextChatContextChat(val name: String, val description: String)
