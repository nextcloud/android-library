/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2017 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.notifications.models

/**
 * Action data model.
 */
class Action() {
    /**
     * Translated short label of the action/button that should be presented to the user.
     */
    @JvmField
    var label: String? = null

    /**
     * A link that should be followed when the action is performed/clicked.
     */
    @JvmField
    var link: String? = null

    /**
     * HTTP method that should be used for the request against the link: GET, POST, DELETE.
     */
    @JvmField
    var type: String? = null

    /**
     * If the action is the primary action for the notification or not.
     */
    @JvmField
    var primary = false

    constructor(label: String?, link: String?, type: String?, primary: Boolean) : this() {
        this.label = label
        this.link = link
        this.type = type
        this.primary = primary
    }
}
