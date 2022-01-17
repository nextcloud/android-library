package com.nextcloud.common

import okhttp3.Dns
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.HashMap

/**
 * DNS Cache which prefers IPv6 unless otherwise specified
 */
object DNSCache {
    data class DNSInfo(val addresses: List<InetAddress>, val preferIPV4: Boolean = false)

    private val cache: MutableMap<String, DNSInfo> = HashMap()

    @Throws(UnknownHostException::class)
    @Synchronized
    fun lookup(hostname: String): List<InetAddress> {
        val entry = cache[hostname]
        if (entry?.addresses?.isNotEmpty() == true) {
            return entry.addresses
        }
        val preferIPV4 = when (entry) {
            null -> false
            else -> entry.preferIPV4
        }

        val addresses = Dns.SYSTEM.lookup(hostname).toMutableList()
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
     * Check whether we have addresses for a hostname, and the first one is IPv6
     */
    @Synchronized
    fun isIPV6(hostname: String): Boolean {
        return cache[hostname]?.addresses?.firstOrNull() is Inet6Address
    }

    /**
     * Clears the cache
     */
    @Synchronized
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
