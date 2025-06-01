/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.e2ee

enum class SignatureAlgorithm(
    val signatureAlg: String,
    val digestAlg: String
) {
    SHA256("SHA256WITHRSAENCRYPTION", "SHA-256")
}
