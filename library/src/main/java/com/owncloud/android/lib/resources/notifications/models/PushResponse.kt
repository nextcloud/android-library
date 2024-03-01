/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2017 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.notifications.models

class PushResponse() {
    var publicKey: String? = null
    var deviceIdentifier: String? = null
    var signature: String? = null

    @Suppress("unused") // used by json parser
    constructor(publicKey: String?, deviceIdentifier: String?, signature: String?) : this() {
        this.publicKey = publicKey
        this.deviceIdentifier = deviceIdentifier
        this.signature = signature
    }
}
