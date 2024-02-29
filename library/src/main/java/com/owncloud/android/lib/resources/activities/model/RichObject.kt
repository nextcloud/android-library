/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-FileCopyrightText: 2017 Alejandro Bautista <aleister09@gmail.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.activities.model

/**
 * RichObject Data Model
 */
class RichObject() {
    var type: String? = null
    var id: String? = null
    var name: String? = null
    var path: String? = null
    var link: String? = null
    var tag: String? = null

    @Suppress("LongParameterList")
    constructor(type: String?, id: String?, name: String?, path: String?, link: String?, tag: String?) : this() {
        this.type = type
        this.id = id
        this.name = name
        this.path = path
        this.link = link
        this.tag = tag
    }
}
