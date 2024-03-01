/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2017 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.notifications.models

/**
 * Rich object data model providing rich data to be used within rich content, e.g.
 * in [Notification]s.
 */
class RichObject() {
    /**
     * A unique identifier for the object type.
     */
    var type: String? = null

    /**
     * A short identifier of the object on the server (int or string).
     */
    @JvmField
    var id: String? = null

    /**
     * A name which should be used in the visual representation.
     */
    var name: String? = null

    constructor(type: String?, id: String?, name: String?) : this() {
        this.type = type
        this.id = id
        this.name = name
    }
}
