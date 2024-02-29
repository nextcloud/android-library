/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-FileCopyrightText: 2017 Joas Schilling <coding@schilljs.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.activities.models

/**
 * PreviewObject Data Model
 */
class PreviewObject() {
    var fileId = 0
    var source: String? = null
    var link: String? = null
    var mimeTypeIcon: Boolean? = null
    var mimeType: String? = null
    var view: String? = null
    var filename: String? = null

    @Suppress("LongParameterList")
    constructor(
        fileId: Int,
        source: String?,
        link: String?,
        mimeTypeIcon: Boolean?,
        mimeType: String?,
        view: String?,
        filename: String?
    ) : this() {
        this.fileId = fileId
        this.source = source
        this.link = link
        this.mimeTypeIcon = mimeTypeIcon
        this.mimeType = mimeType
        this.view = view
        this.filename = filename
    }
}
