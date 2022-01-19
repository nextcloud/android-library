/*
 * Nextcloud Android client application
 *
 * @author Tobias Kaminsky
 * Copyright (C) 2021 Tobias Kaminsky
 * Copyright (C) 2021 Nextcloud GmbH
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *  
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 *  
 */

package com.owncloud.android

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import javax.net.ssl.SSLHandshakeException

abstract class ClientIT {
    val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    // 🎫Certificate
    @Test(expected = SSLHandshakeException::class)
    fun expiredCert() {
        testConnection("https://expired.badssl.com")
    }

    @Test(expected = SSLHandshakeException::class)
    fun wrongHostCert() {
        testConnection("https://wrong.host.badssl.com")
    }

    @Test(expected = SSLHandshakeException::class)
    fun selfSignedCert() {
        testConnection("https://self-signed.badssl.com")
    }

    @Test(expected = SSLHandshakeException::class)
    fun untrustedRootCert() {
        testConnection("https://untrusted-root.badssl.com")
    }

    @Test(expected = SSLHandshakeException::class)
    fun revokedCert() {
        testConnection("https://revoked.badssl.com")
    }

    @Test(expected = SSLHandshakeException::class)
    fun pinningTestCert() {
        testConnection("https://pinning-test.badssl.com")
    }

    @Test(expected = SSLHandshakeException::class)
    fun noCommonName() {
        testConnection("https://no-common-name.badssl.com")
    }

    @Test(expected = SSLHandshakeException::class)
    fun noSubject() {
        testConnection("https://no-subject.badssl.com")
    }

    @Test(expected = SSLHandshakeException::class)
    fun incompleteChain() {
        testConnection("https://incomplete-chain.badssl.com")
    }

    @Test
    fun sha256() {
        testConnection("https://sha256.badssl.com")
    }

    @Test
    fun sha384() {
        testConnection("https://sha384.badssl.com")
    }

    @Test
    fun sha512() {
        testConnection("https://sha512.badssl.com")
    }

    @Test
    fun sans1000() {
        testConnection("https://1000-sans.badssl.com")
    }

    @Test
    fun sans10000() {
        testConnection("https://10000-sans.badssl.com")
    }

    @Test
    fun ecc256() {
        testConnection("https://ecc-256-sans.badssl.com")
    }

    @Test
    fun ecc384() {
        testConnection("https://ecc-384-sans.badssl.com")
    }

    @Test
    fun rsa2048() {
        testConnection("https://rsa2048.badssl.com")
    }

    @Test
    fun rsa4096() {
        testConnection("https://rsa4096.badssl.com")
    }

    @Test
    fun rsa8192() {
        testConnection("https://rsa8192.badssl.com")
    }

    @Test
    fun extendedValidation() {
        testConnection("https://extended-validation.badssl.com")
    }

    // 🎟Client Certificate
    // not tested

    // 🖼Mixed Content
    // TODO shall we?

    // ✏️HTTP
    // TODO shall we?

    // 🔀Cipher Suite
    @Test
    fun cbc() {
        testConnection("https://cbc.badssl.com")
    }

    @Test(expected = SSLHandshakeException::class)
    fun rc4md5() {
        testConnection("https://rc4-md5.badssl.com")
    }

    @Test(expected = SSLHandshakeException::class)
    fun rc4() {
        testConnection("https://rc4.badssl.com")
    }

    @Test(expected = SSLHandshakeException::class)
    fun des3() {
        testConnection("https://3des.badssl.com")
    }

    @Test(expected = SSLHandshakeException::class)
    fun testNull() {
        testConnection("https://null.badssl.com")
    }

    @Test(expected = SSLHandshakeException::class)
    fun mozillaOld() {
        testConnection("https://mozilla-old.badssl.com")
    }

    @Test
    fun mozillaIntermediate() {
        testConnection("https://mozilla-intermediate.badssl.com")
    }

    @Test
    fun mozillaModern() {
        testConnection("https://mozilla-modern.badssl.com")
    }

    // 🔑Key Exchange
    @Test(expected = SSLHandshakeException::class)
    fun dh480() {
        testConnection("https://dh480.badssl.com")
    }

    @Test(expected = SSLHandshakeException::class)
    fun dh512() {
        testConnection("https://dh512.badssl.com")
    }

    @Test(expected = SSLHandshakeException::class)
    fun dh1024() {
        testConnection("https://dh1024.badssl.com")
    }

    @Test(expected = SSLHandshakeException::class)
    fun dh2048() {
        testConnection("https://dh2048.badssl.com")
    }

    @Test(expected = SSLHandshakeException::class)
    fun dhSmallSubgroup() {
        testConnection("https://dh-small-subgroup.badssl.com")
    }

    @Test(expected = SSLHandshakeException::class)
    fun dhComposite() {
        testConnection("https://dh-composite.badssl.com")
    }

    @Test
    fun staticRSA() {
        testConnection("https://static-rsa.badssl.com")
    }

    // ↔️Protocol
    @Test
    fun tls1() {
        testConnection("https://tls-v1-0.badssl.com:1010")
    }

    @Test
    fun tls11() {
        testConnection("https://tls-v1-1.badssl.com:1011")
    }

    @Test
    fun tls12() {
        testConnection("https://tls-v1-2.badssl.com:1012")
    }

    // 🔍Certificate Transparency
    @Test(expected = SSLHandshakeException::class)
    fun noSCT() {
        testConnection("https://no-sct.badssl.com")
    }

    // ⬆️Upgrade
    @Test
    fun hsts() {
        testConnection("https://hsts.badssl.com")
    }

    @Test
    fun upgrade() {
        testConnection("https://upgrade.badssl.com")
    }

    @Test
    fun preloadedHsts() {
        testConnection("https://preloaded-hsts.badssl.com")
    }

    @Test(expected = SSLHandshakeException::class)
    fun subdomainPreloadedHsts() {
        testConnection("https://subdomain.preloaded-hsts.badssl.com")
    }

    // 👀UI
    @Test
    fun longTitle() {
        testConnection("https://long-extended-subdomain-name-containing-many-letters-and-dashes.badssl.com")
    }

    @Test
    fun longTitle2() {
        testConnection("https://longextendedsubdomainnamewithoutdashesinordertotestwordwrapping.badssl.com")
    }

    // ❌Known Bad

    @Test(expected = SSLHandshakeException::class)
    fun superfish() {
        testConnection("https://superfish.badssl.com")
    }

    @Test(expected = SSLHandshakeException::class)
    fun eDellRoot() {
        testConnection("https://edellroot.badssl.com")
    }

    @Test(expected = SSLHandshakeException::class)
    fun dsdTestProvider() {
        testConnection("https://dsdtestprovider.badssl.com")
    }

    @Test(expected = SSLHandshakeException::class)
    fun preactCLI() {
        testConnection("https://preact-cli.badssl.com")
    }

    @Test(expected = SSLHandshakeException::class)
    fun webpackDevServer() {
        testConnection("https://webpack-dev-server.badssl.com")
    }

    // Chrome Tests
    @Test(expected = SSLHandshakeException::class)
    fun captivePortal() {
        testConnection("https://captive-portal.badssl.com")
    }

    @Test(expected = SSLHandshakeException::class)
    fun mitmSoftware() {
        testConnection("https://mitm-software.badssl.com")
    }

    // ☠️Defunct
    @Test(expected = SSLHandshakeException::class)
    fun sha1_2016() {
        testConnection("https://sha1-2016.badssl.com")
    }

    @Test(expected = SSLHandshakeException::class)
    fun sha1_2017() {
        testConnection("https://sha1-2017.badssl.com")
    }

    @Test(expected = SSLHandshakeException::class)
    fun sha1_intermediate() {
        testConnection("https://sha1-intermediate.badssl.com")
    }

    @Test(expected = SSLHandshakeException::class)
    fun invalidExpectedSCT() {
        testConnection("https://invalid-expected-sct.badssl.com")
    }

    abstract fun testConnection(url: String)
}
