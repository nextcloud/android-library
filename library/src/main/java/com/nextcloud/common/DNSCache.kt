/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2022-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2025 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.common

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.VisibleForTesting
import com.nextcloud.android.lib.core.Clock
import com.nextcloud.android.lib.core.ClockImpl
import okhttp3.Dns
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.ConcurrentHashMap

/**
 * DNS Cache which prefers IPv6 unless otherwise specified
 */
object DNSCache {
    const val DEFAULT_TTL = 30 * 1000L

    // 30 seconds is the Java default. Let's keep it.
    @VisibleForTesting
    @Volatile
    var ttlMillis: Long = DEFAULT_TTL

    @VisibleForTesting
    @Volatile
    var clock: Clock = ClockImpl()

    @VisibleForTesting
    @Volatile
    var dns: Dns = Dns.SYSTEM

    data class DNSInfo(
        val addresses: List<InetAddress>,
        val preferIPV4: Boolean = false,
        val timestamp: Long = clock.currentTimeMillis
    ) {
        fun isExpired(): Boolean = clock.currentTimeMillis - timestamp > ttlMillis
    }

    private val cache: ConcurrentHashMap<String, DNSInfo> = ConcurrentHashMap()

    @Throws(UnknownHostException::class)
    @JvmStatic
    fun lookup(hostname: String): List<InetAddress> {
        val entry = cache[hostname]
        if (entry?.addresses?.isNotEmpty() == true && !entry.isExpired()) {
            return entry.addresses
        }

        val addresses = dns.lookup(hostname).toMutableList()
        if (addresses.isEmpty()) {
            throw UnknownHostException("Unknown host $hostname")
        }

        val preferIPV4 = entry?.preferIPV4 ?: false
        val sortedAddresses = sortAddresses(addresses, preferIPV4)
        cache[hostname] = DNSInfo(sortedAddresses, preferIPV4)

        return sortedAddresses
    }

    /**
     * Set IP version preference for a hostname, and re-sort addresses if needed
     */
    @JvmStatic
    fun setIPVersionPreference(
        hostname: String,
        preferIPV4: Boolean
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            cache.compute(hostname) { _, old ->
                val addresses =
                    old?.addresses?.let {
                        sortAddresses(it, preferIPV4)
                    } ?: emptyList()
                DNSInfo(addresses, preferIPV4)
            }
        } else {
            val addresses = cache[hostname]?.addresses?.let { sortAddresses(it, preferIPV4) } ?: emptyList()
            cache[hostname] = DNSInfo(addresses, preferIPV4)
        }
    }

    /**
     * Only returns <code>true</code> if, for a given address all of the following is true:
     *  - There are saved IP addresses for the hostname
     *  - The first address is an IPv6 address
     *  - There are IPv4 addresses available too
     */
    @JvmStatic
    fun isIPV6First(hostname: String): Boolean {
        val firstV6 = cache[hostname]?.addresses?.firstOrNull() is Inet6Address
        val anyV4 = cache[hostname]?.addresses?.any { it is Inet4Address } == true
        return firstV6 && anyV4
    }

    /**
     * Clears the cache
     */
    @JvmStatic
    fun clear() {
        cache.clear()
    }

    private fun sortAddresses(
        addresses: List<InetAddress>,
        preferIPV4: Boolean
    ): List<InetAddress> =
        addresses.sortedWith { address1, _ ->
            val order =
                when (address1) {
                    is Inet4Address -> 1
                    else -> -1
                }
            when (preferIPV4) {
                true -> order * -1
                else -> order
            }
        }
}
