/*
 *  Nextcloud Android Library is available under MIT license
 *
 *  @author Álvaro Brey Vilas
 *  Copyright (C) 2022 Álvaro Brey Vilas
 *  Copyright (C) 2022 Nextcloud GmbH
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 *  BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 *  ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package com.nextcloud.common

import androidx.annotation.VisibleForTesting
import com.nextcloud.android.lib.core.Clock
import com.nextcloud.android.lib.core.ClockImpl
import okhttp3.Dns
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.net.UnknownHostException

/**
 * DNS Cache which prefers IPv6 unless otherwise specified
 */
object DNSCache {

    const val DEFAULT_TTL = 30 * 1000L

    // 30 seconds is the Java default. Let's keep it.
    @VisibleForTesting
    var ttlMillis: Long = DEFAULT_TTL

    @VisibleForTesting
    var clock: Clock = ClockImpl()

    @VisibleForTesting
    var dns: Dns = Dns.SYSTEM

    data class DNSInfo(
        val addresses: List<InetAddress>,
        val preferIPV4: Boolean = false,
        val timestamp: Long = clock.currentTimeMillis
    ) {
        fun isExpired(): Boolean = clock.currentTimeMillis - timestamp > ttlMillis
    }

    private val cache: MutableMap<String, DNSInfo> = HashMap()

    @Throws(UnknownHostException::class)
    @Synchronized
    @JvmStatic
    fun lookup(hostname: String): List<InetAddress> {
        val entry = cache[hostname]
        if (entry?.addresses?.isNotEmpty() == true && !entry.isExpired()) {
            return entry.addresses
        }
        val preferIPV4 = when (entry) {
            null -> false
            else -> entry.preferIPV4
        }

        val addresses = dns.lookup(hostname).toMutableList()
        if (addresses.isEmpty()) {
            throw UnknownHostException("Unknown host $hostname")
        }
        val sortedAddresses = sortAddresses(addresses, preferIPV4)

        val newEntry = DNSInfo(sortedAddresses, preferIPV4)
        cache[hostname] = newEntry

        return sortedAddresses
    }

    /**
     * Set IP version preference for a hostname, and re-sort addresses if needed
     */
    @Synchronized
    @JvmStatic
    fun setIPVersionPreference(hostname: String, preferIPV4: Boolean) {
        val entry = cache[hostname]
        if (entry != null) {
            val addresses = sortAddresses(entry.addresses, preferIPV4)
            cache[hostname] = DNSInfo(addresses, preferIPV4)
        } else {
            cache[hostname] = DNSInfo(emptyList(), preferIPV4)
        }
    }

    /**
     * Only returns <code>true</code> if, for a given address all of the following is true:
     *  - There are saved IP addresses for the hostname
     *  - The first address is an IPv6 address
     *  - There are IPv4 addresses available too
     */
    @Synchronized
    @JvmStatic
    fun isIPV6First(hostname: String): Boolean {
        val firstV6 = cache[hostname]?.addresses?.firstOrNull() is Inet6Address
        val anyV4 = cache[hostname]?.addresses?.any { it is Inet4Address } == true
        return firstV6 && anyV4
    }

    /**
     * Clears the cache
     */
    @Synchronized
    @JvmStatic
    fun clear() {
        cache.clear()
    }

    private fun sortAddresses(
        addresses: List<InetAddress>,
        preferIPV4: Boolean
    ): List<InetAddress> = addresses.sortedWith { address1, _ ->
        val order = when (address1) {
            is Inet4Address -> 1
            else -> -1
        }
        when (preferIPV4) {
            true -> order * -1
            else -> order
        }
    }
}
