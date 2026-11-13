/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.externalstorage.model

/**
 * Auth mechanisms registered by External Storage app.
 * Represents the way an external storage mount is authenticated.
 *
 * I.e.:
 * PasswordUserProvided --> User has to provide its credentials the first time the share is opened
 * PasswordGlobal --> The admin provides the credentials and the user finds the share already mounted
 * etc...
 */
enum class AuthMechanism(
    val identifier: String
) {
    Null("null::null"),
    Builtin("builtin::builtin"),
    Password("password::password"),
    PasswordSessionCredentials("password::sessioncredentials"),
    PasswordLoginCredentials("password::logincredentials"),
    PasswordUserProvided("password::userprovided"),
    PasswordGlobal("password::global"),
    PasswordGlobalUser("password::global::user"),
    PublicKeyRsa("publickey::rsa"),
    PublicKeyRsaPrivate("publickey::rsa_private"),
    AmazonS3AccessKey("amazons3::accesskey"),
    OpenStack("openstack::openstack"),
    OpenStackV3("openstack::openstackv3"),
    OpenStackRackspace("openstack::rackspace"),
    SmbKerberos("smb::kerberos"),
    SmbKerberosApache("smb::kerberosapache"),
    Unknown("");

    companion object {
        fun fromIdentifier(identifier: String): AuthMechanism =
            entries.firstOrNull { it != Unknown && it.identifier == identifier } ?: Unknown
    }
}
