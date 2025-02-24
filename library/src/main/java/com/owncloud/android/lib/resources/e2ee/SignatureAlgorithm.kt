/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.e2ee

enum class SignatureAlgorithm(val signatureAlg: String, val digestAlg: String) {
    SHA1("SHA1WITHRSA", "SHA-1"),
    SHA256("SHA256WITHRSA", "SHA-256")
}
