package com.nextcloud.common

import com.nextcloud.android.lib.core.ClockImpl
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase
import okhttp3.Dns
import org.junit.Test
import java.net.InetAddress

class DNSCacheTest : TestCase() {

    companion object {
        private const val TEST_HOST = "test.localhost"
        private val TEST_IPV4 =
            InetAddress.getByAddress(TEST_HOST, byteArrayOf(127, 0, 0, 1))
        private val TEST_IPV6 = InetAddress.getByAddress(
            TEST_HOST,
            byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)
        )
    }

    private fun setStaticClock() {
        DNSCache.clock = ClockStub(currentTimeValue = 1000)
    }

    override fun setUp() {
        setStaticClock()
    }

    public override fun tearDown() {
        DNSCache.ttlMillis = DNSCache.ttlMillis
        DNSCache.clock = ClockImpl()
        DNSCache.dns = Dns.SYSTEM
        DNSCache.clear()
    }

    private fun compareLookupLists(expected: List<InetAddress>, actual: List<InetAddress>) {
        assertEquals("Wrong address list size", expected.size, actual.size)
        for (i in expected.indices) {
            assertEquals("Wrong address at position $i", expected[i], actual[i])
        }
    }

    @Test
    fun testDnsCache_onlyIPv4() {
        val dns: Dns = mock()
        val addressList = listOf(TEST_IPV4)
        whenever(dns.lookup(any())) doReturn addressList
        DNSCache.dns = dns

        val result = DNSCache.lookup(TEST_HOST)

        compareLookupLists(addressList, result)
    }

    @Test
    fun testDnsCache_onlyIPv6() {
        val dns: Dns = mock()
        val addressList = listOf(TEST_IPV6)
        whenever(dns.lookup(any())) doReturn addressList
        DNSCache.dns = dns

        val result = DNSCache.lookup(TEST_HOST)

        compareLookupLists(addressList, result)
    }

    @Test
    fun testDnsCache_multipleAddresses() {
        val dns: Dns = mock()
        val addressList = listOf(TEST_IPV4, TEST_IPV6)
        whenever(dns.lookup(any())) doReturn addressList
        DNSCache.dns = dns

        // on first lookup ipv6 should be preferred
        compareLookupLists(listOf(TEST_IPV6, TEST_IPV4), DNSCache.lookup(TEST_HOST))
        assertEquals(true, DNSCache.isIPV6First(TEST_HOST))

        // set ipv4 preference
        DNSCache.setIPVersionPreference(TEST_HOST, true)
        assertEquals(false, DNSCache.isIPV6First(TEST_HOST))
        compareLookupLists(listOf(TEST_IPV4, TEST_IPV6), DNSCache.lookup(TEST_HOST))

        // set ipv6 again
        DNSCache.setIPVersionPreference(TEST_HOST, false)
        assertEquals(true, DNSCache.isIPV6First(TEST_HOST))
        compareLookupLists(listOf(TEST_IPV6, TEST_IPV4), DNSCache.lookup(TEST_HOST))
    }

    @Test
    fun testDNSEntry_expired() {
        DNSCache.ttlMillis = 50

        val staticClockValue = DNSCache.clock.currentTimeMillis

        val expiredEntry = DNSCache.DNSInfo(emptyList(), false, staticClockValue - 200)
        assertTrue("Entry should be expired", expiredEntry.isExpired())

        val nonExpiredEntry = DNSCache.DNSInfo(emptyList(), false, staticClockValue - 10)
        assertFalse("Entry should not be expired", nonExpiredEntry.isExpired())
    }

    @Test
    fun testDNSLookupExpiration() {
        val dns: Dns = mock()
        val initialList = listOf(TEST_IPV4)
        val secondList = listOf(TEST_IPV6)
        whenever(dns.lookup(any())) doReturn initialList
        DNSCache.dns = dns

        DNSCache.clock = ClockStub(currentTimeValue = 1000)
        DNSCache.ttlMillis = 500

        // initial lookup
        compareLookupLists(initialList, DNSCache.lookup(TEST_HOST))

        // change DNS response upstream
        whenever(dns.lookup(any())) doReturn secondList

        // increase clock by less than expiration TTL, same initial list is expected
        DNSCache.clock = ClockStub(currentTimeValue = 1100)
        compareLookupLists(initialList, DNSCache.lookup(TEST_HOST))

        // increase clock so that lookup expires, and change IP list to sense the change
        DNSCache.clock = ClockStub(currentTimeValue = 1501)
        compareLookupLists(secondList, DNSCache.lookup(TEST_HOST))
    }
}
