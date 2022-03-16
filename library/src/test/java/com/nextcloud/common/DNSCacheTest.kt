package com.nextcloud.common

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase
import okhttp3.Dns
import org.junit.Assert
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

    public override fun tearDown() {
        DNSCache.dns = Dns.SYSTEM
        DNSCache.clear()
    }

    private fun compareLookupLists(expected: List<InetAddress>, actual: List<InetAddress>) {
        Assert.assertEquals("Wrong address list size", expected.size, actual.size)
        for (i in expected.indices) {
            Assert.assertEquals("Wrong address at position $i", expected[i], actual[i])
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
        Assert.assertEquals(true, DNSCache.isIPV6First(TEST_HOST))

        // set ipv4 preference
        DNSCache.setIPVersionPreference(TEST_HOST, true)
        Assert.assertEquals(false, DNSCache.isIPV6First(TEST_HOST))
        compareLookupLists(listOf(TEST_IPV4, TEST_IPV6), DNSCache.lookup(TEST_HOST))

        // set ipv6 again
        DNSCache.setIPVersionPreference(TEST_HOST, false)
        Assert.assertEquals(true, DNSCache.isIPV6First(TEST_HOST))
        compareLookupLists(listOf(TEST_IPV6, TEST_IPV4), DNSCache.lookup(TEST_HOST))
    }
}
