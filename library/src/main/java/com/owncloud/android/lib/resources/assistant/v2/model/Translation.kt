/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package com.owncloud.android.lib.resources.assistant.v2.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class TranslationLanguage(
    val name: String,
    val code: String
)

@Serializable
data class TranslationRequest(
    @SerialName("origin_language")
    val originLanguage: String,
    @SerialName("max_tokens")
    val maxTokens: Double,
    val model: String,
    @SerialName("target_language")
    val targetLanguage: String,
    val input: String
)

data class TranslationModel(
    val model: String,
    val maxTokens: Double
)

data class TranslationLanguages(
    val originLanguages: List<TranslationLanguage>,
    val targetLanguages: List<TranslationLanguage>
)

fun TaskTypeData.toTranslationLanguages(): TranslationLanguages {
    fun List<EnumValue>?.toTranslationLanguageList() =
        this
            .orEmpty()
            .map { TranslationLanguage(it.name, it.value) }

    return TranslationLanguages(
        originLanguages = inputShapeEnumValues?.get("origin_language").toTranslationLanguageList(),
        targetLanguages = inputShapeEnumValues?.get("target_language").toTranslationLanguageList()
    )
}

fun TaskTypeData.toTranslationModel(): TranslationModel? {
    val model = optionalInputShapeDefaults?.get("model") as? String
    val maxTokens = optionalInputShapeDefaults?.get("max_tokens") as? Double
    return if (model != null && maxTokens != null) TranslationModel(model, maxTokens) else null
}
